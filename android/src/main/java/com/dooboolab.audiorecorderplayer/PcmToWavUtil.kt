import android.media.AudioFormat
import android.media.AudioRecord
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class PcmToWavUtil {
    private var mBufferSize //缓存的音频大小
            : Int
    private var mSampleRate = 8000 // 8000|16000
    private var mChannelConfig = AudioFormat.CHANNEL_IN_STEREO //立体声
    private var mChannelCount = 2
    private var mEncoding = AudioFormat.ENCODING_PCM_16BIT

    constructor() {
        mBufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannelConfig, mEncoding)
    }

    /**
     * @param sampleRate sample rate、采样率
     * @param channelConfig    channel、声道
     * @param encoding   Audio data format、音频格式
     */
    constructor(sampleRate: Int, channelConfig: Int, channelCount: Int, encoding: Int) {
        mSampleRate = sampleRate
        mChannelConfig = channelConfig
        mChannelCount = channelCount
        mEncoding = encoding
        mBufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannelConfig, mEncoding)
    }

    /**
     * pcm文件转wav文件
     *
     * @param inFilename  源文件路径
     * @param outFilename 目标文件路径
     */
    fun pcmToWav(inFilename: String?, outFilename: String?) {
        val `in`: FileInputStream
        val out: FileOutputStream
        val totalAudioLen: Long
        val totalDataLen: Long
        val longSampleRate = mSampleRate.toLong()
        val channels = mChannelCount
        val byteRate = (16 * mSampleRate * channels / 8).toLong()
        val data = ByteArray(mBufferSize)
        try {
            `in` = FileInputStream(inFilename)
            out = FileOutputStream(outFilename)
            totalAudioLen = `in`.channel.size()
            totalDataLen = totalAudioLen + 36 //44-8(RIFF+dadasize(4个字节))
            writeWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate)
            while (`in`.read(data) != -1) {
                out.write(data)
            }
            `in`.close()
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 加入wav文件头
     */
    @Throws(IOException::class)
    private fun writeWaveFileHeader(out: FileOutputStream, totalAudioLen: Long,
                                    totalDataLen: Long, longSampleRate: Long, channels: Int, byteRate: Long) {
        val header = ByteArray(44)
        header[0] = 'R'.code.toByte() // RIFF/WAVE header
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xffL).toByte()
        header[5] = (totalDataLen shr 8 and 0xffL).toByte()
        header[6] = (totalDataLen shr 16 and 0xffL).toByte()
        header[7] = (totalDataLen shr 24 and 0xffL).toByte()
        header[8] = 'W'.code.toByte() //WAVE
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte() // 'fmt ' chunk
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // 4 bytes: size of 'fmt ' chunk
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // format = 1
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (longSampleRate and 0xffL).toByte()
        header[25] = (longSampleRate shr 8 and 0xffL).toByte()
        header[26] = (longSampleRate shr 16 and 0xffL).toByte()
        header[27] = (longSampleRate shr 24 and 0xffL).toByte()
        header[28] = (byteRate and 0xffL).toByte()
        header[29] = (byteRate shr 8 and 0xffL).toByte()
        header[30] = (byteRate shr 16 and 0xffL).toByte()
        header[31] = (byteRate shr 24 and 0xffL).toByte()
        header[32] = (2 * 16 / 8).toByte() // block align
        header[33] = 0
        header[34] = 16 // bits per sample
        header[35] = 0
        header[36] = 'd'.code.toByte() //data
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xffL).toByte()
        header[41] = (totalAudioLen shr 8 and 0xffL).toByte()
        header[42] = (totalAudioLen shr 16 and 0xffL).toByte()
        header[43] = (totalAudioLen shr 24 and 0xffL).toByte()
        out.write(header, 0, 44)
    }
}