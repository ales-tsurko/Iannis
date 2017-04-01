IannisRecorder : CompositeView {
  var currentPlayerSynth, 
  <currentBuffer,
  <currentFilePath,
  <isPlaying,
  <>inputBusNum, <>quant,
  <dir;

  *new {arg dir;
    ^super.new.init(dir);
  }

  init {arg samplesDir;
    dir = samplesDir;
    inputBusNum = 0;
    quant = 4;
    isPlaying = false;

    SynthDef(\iannis_mic_in, {arg in, out;
      Out.ar(out, SoundIn.ar(in));
    }).add();
  }

  prepareForRecord {
    currentFilePath = dir +/+ ("Recording-"++thisThread.seconds.asString).replace(".", "-") ++ ".aif";
    // Server.default.prepareForRecord(currentFilePath, 2);
  }

  record {
    Routine.run({
      this.prepareForRecord();

      // start recording
      Server.default.record(currentFilePath, inputBusNum, 2);
    }, quant: this.quant);
  }

  stopRecording {
    Routine.run({
      // stop recording
      Server.default.stopRecording();

      // init current buffer
      currentBuffer = Buffer.read(Server.default, currentFilePath);
    }, quant: this.quant);  
  }

  playSample {arg isLoop = false;
    if(isPlaying.not && Server.default.isRecording.not) {
      Routine.run({
        currentPlayerSynth = currentBuffer.play(isLoop);
        isPlaying = isLoop;
      }, quant: this.quant);
    }
  }

  stopSample {
    if (isPlaying && Server.default.isRecording.not) {
      Routine.run({
        currentPlayerSynth.free();
        isPlaying = false;
      }, quant: this.quant);
    }
  }

}
