package V4S4J.V4S4J

import com.sun.jna.ptr.{IntByReference, PointerByReference}
import dev.capslock.voicevoxcore4s.Hello.{core, dictionaryDirectory, initialized}
import dev.capslock.voicevoxcore4s.{Core, Util}

import java.io.FileOutputStream
import java.nio.file.Path

object V4S4J extends App {
  def init(path: String, islinux: Boolean): Unit = {
    // Extract dictionary files from JAR into real file system.
    // Files will go to temporary directory in OS.
    val dictionaryDirectory = Util.extractDictFiles()

    // Load required library
    com.sun.jna.NativeLibrary.addSearchPath("voicevox_core", path)
    com.sun.jna.NativeLibrary.addSearchPath("onnxruntime", path)
    if (islinux) System.load(Path.of(path).resolve("libonnxruntime.so.1.13.1").toAbsolutePath.toString)

    // vocevox_core will be loaded automatically.
    val core = Core()
    println(core.voicevox_get_version())

    val initializeOptions = core.voicevox_make_default_initialize_options()
    initializeOptions.open_jtalk_dict_dir = dictionaryDirectory
    initializeOptions.acceleration_mode =
      Core.VoicevoxAccelerationMode.VOICEVOX_ACCELERATION_MODE_CPU.code
    println(initializeOptions)

    val initialized = core.voicevox_initialize(initializeOptions)
    println(s"VoiceVoxInit, voicevoxcore4s! initialized? -> ($initialized)")

  }

  def tts(m:String): Boolean = {
    if (initialized == Core.VoicevoxResultCode.VOICEVOX_RESULT_OK.code) {
      val model_shikoku_metan = 2
      core.voicevox_load_model(model_shikoku_metan)

      // Generating voice.
      // First, we should have two pointers: result length, result wav buffer.
      val bytesLength = new IntByReference()
      val wavBuffer = new PointerByReference()

      // Second, prepare TTS(talk to speech) options
      val ttsOpts = core.voicevox_make_default_tts_options()
      ttsOpts.kana = false

      // Run TTS.
      // VOICEVOX will rewrite memory beyond pointer.
      val tts = core.voicevox_tts(
        m,
        model_shikoku_metan,
        ttsOpts,
        bytesLength, // this will be modified
        wavBuffer // this will be modified
      )
      if (tts == Core.VoicevoxResultCode.VOICEVOX_RESULT_OK.code) {
        // You can acquire data from pointer using getValue()
        val resultPtr = wavBuffer.getValue()
        val resultArray = resultPtr.getByteArray(0, bytesLength.getValue())

        // Write out buffer.
        val fs = new FileOutputStream("./result.wav")
        fs.write(resultArray)
        fs.close()

        // Release allocated memory.
        core.voicevox_wav_free(resultPtr)
        return true
      }
    }
    false
  }

  def fin(): Unit = {
    // When program exit, bury VOICEVOX instance.
    core.voicevox_finalize()

    // Delete dictionary directory extracted into temporary directory.
    os.remove.all(os.Path(dictionaryDirectory))
  }
}