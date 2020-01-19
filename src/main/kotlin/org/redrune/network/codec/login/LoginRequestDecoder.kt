package org.redrune.network.codec.login

import com.alex.utils.Utils
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.redrune.cache.Cache
import org.redrune.network.NetworkConstants
import org.redrune.network.NetworkSession
import org.redrune.network.codec.packet.RS2PacketDecoder
import org.redrune.network.packet.outgoing.login.LobbyBuilderPacket
import org.redrune.network.packet.outgoing.login.LoginResponsePacket
import org.redrune.util.LoginReturnCode
import org.redrune.util.crypto.IsaacRandom
import org.redrune.util.crypto.IsaacRandomPair
import org.redrune.util.func.TextFunc
import org.redrune.util.func.protocolFormat
import util.buffer.FixedBuffer
import java.util.*

class LoginRequestDecoder : ByteToMessageDecoder() {

    private var session: NetworkSession? = null
        set(value) {
            field = value
            field?.channel?.attr(NetworkConstants.SESSION_KEY)?.set(field);
        }

    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        if (buf.readableBytes() < 3) {
            return
        }
        val opcode = buf.readUnsignedByte().toInt()
        val size = buf.readUnsignedShort()
        if (buf.readableBytes() != size) {
            ctx.close()
            return
        }
        if (opcode != 16 && opcode != 18 && opcode != 19) {
            println("Received unexpected world login opcode: $opcode")
            session = NetworkSession(ctx.channel())
            session?.write(LoginResponsePacket(LoginReturnCode.BETA_TESTERS_ONLY))
                    ?.addListener(ChannelFutureListener.CLOSE)
            return
        }
        val revision = buf.readInt()
        if (revision != NetworkConstants.PROTOCOL_NUMBER) {
            println("Received unexpected protocol number: $revision")
            session = NetworkSession(ctx.channel())
            session?.write(LoginResponsePacket(LoginReturnCode.BETA_TESTERS_ONLY))
                    ?.addListener(ChannelFutureListener.CLOSE)
            return
        }
        session = NetworkSession(ctx.channel())
        val data = ByteArray(size - 4)
        // store the data into the buffer
        buf.readBytes(data)
        // convert the buffer into a readable object
        val buffer = FixedBuffer(data)
        when (opcode) {
            19 -> {
                decodeLobbyLogin(ctx, buffer, out)
            }
            16 -> {
                println("Decode world!!")
                decodeWorldLogin(ctx, buffer, out)
            }
            else -> {
                println("Received unexpected login request from $session. [opcode=$opcode]")
                ctx.channel().close()
            }
        }
    }

    private fun decodeLobbyLogin(ctx: ChannelHandlerContext, buffer: FixedBuffer, out: MutableList<Any>) {
        session?.state = NetworkSession.SessionState.LOBBY_DECODING
        val rsaSize = buffer.readUnsignedShort()
        if (rsaSize > buffer.remaining) {
            sendResponse(LoginReturnCode.BAD_SESSION_ID)
            return
        }
        val rsaData = ByteArray(rsaSize)
        buffer.read(rsaData)
        val rsaBuffer = FixedBuffer(Utils.cryptRSA(rsaData, NetworkConstants.LOGIN_RSA_PRIVATE, NetworkConstants.LOGIN_RSA_MODULUS))
        if (rsaBuffer.readUnsignedByte() != 10) {
            sendResponse(LoginReturnCode.BAD_SESSION_ID)
            return
        }
        val isaacSeed = IntArray(4)
        for (i in isaacSeed.indices) {
            isaacSeed[i] = rsaBuffer.readInt()
        }
        if (rsaBuffer.readLong() != 0L) {
            sendResponse(LoginReturnCode.BAD_SESSION_ID)
            return
        }
        val password: String = rsaBuffer.readString()
        rsaBuffer.readLong()
        rsaBuffer.readLong()
        buffer.decodeXTEA(isaacSeed, buffer.offset, buffer.length)
        val username: String = buffer.readString().protocolFormat()
        val gameType = buffer.readUnsignedByte()
        val language = buffer.readUnsignedByte()
        buffer.skipBefore(24)
        buffer.readString()
        buffer.readInt()
        for (index in 0..35) {
            val crc = if (Cache.indexes[index] == null) 0 else Cache.indexes[index].crc
            val receivedCrc = buffer.readInt()
            if (crc != receivedCrc && index < 32) {
                sendResponse(LoginReturnCode.UPDATED)
                return
            }
        }
        if (TextFunc.invalidAccountName(username)) {
            sendResponse(LoginReturnCode.INVALID_CREDENTIALS)
            return
        }
        /*if (World.getLobbyPlayers().size >= org.redrune.utility.constants.GameConstants.PLAYERS_LIMIT - 10) {
            session!!.write(LoginResponseCodePacketBuilder(org.redrune.utility.game.entity.actor.player.LoginReturnCode.FULL_WORLD)).addListener(ChannelFutureListener.CLOSE)
            return
        }
        if (World.containsPlayer(username, false)) {
            session!!.write(LoginResponseCodePacketBuilder(org.redrune.utility.game.entity.actor.player.LoginReturnCode.ALREADY_ONLINE)).addListener(ChannelFutureListener.CLOSE)
            return
        }

        val player: org.redrune.game.entity.actor.player.Player

        if (!PlayerSaving.playerExists(username)) {
            player = org.redrune.game.entity.actor.player.Player(username)
        } else {
            player = PlayerSaving.fromFile(username)
            if (player == null) {
                sendResponse(LoginReturnCode.INVALID_LOGIN_SERVER)
                return
            }
        }*/
        val inCipher = Arrays.copyOf(isaacSeed, isaacSeed.size)
        val outCipher = IntArray(4)
        for (i in isaacSeed.indices) {
            outCipher[i] = isaacSeed[i] + 50
        }
        session?.isaacPair = IsaacRandomPair(IsaacRandom(inCipher), IsaacRandom(outCipher))
        session?.write(LobbyBuilderPacket())
        session?.state = NetworkSession.SessionState.LOBBY
        ctx.pipeline().replace("decoder", "decoder", session?.let { RS2PacketDecoder(it) })
    }

    private fun decodeWorldLogin(ctx: ChannelHandlerContext, buffer: FixedBuffer, out: MutableList<Any>) {
        TODO("not implemented")
    }

    private fun sendResponse(returnCode: LoginReturnCode) {
        session?.write(LoginResponsePacket(returnCode))?.addListener(ChannelFutureListener.CLOSE)
    }


}