IannisRecorder {
  var delegate,
  playerSynth, recorderSynth,
  startTime, <recordingDur,
  maxDur,
  recordingBuffer, playerBuffer, 
  <>isInputHardware,
  <soundfile,
  <isPlaying,
  <isPlayingLoop,
  <isRecording,
  <>inputBusNum, <>quant,
  <recordingDir, <soundfilesInDir;

  *new {arg recordingDir, delegate;
    ^super.new.init(recordingDir, delegate);
  }

  init {arg samplesDir, viewController;
    delegate = viewController;
    this.recordingDir = samplesDir;
    inputBusNum = 0;
    quant = 4;
    isPlaying = false;
    isRecording = false;
    this.maxDur = 30;
    soundfile = SoundFile.new;
    isInputHardware = false;
    isPlayingLoop = false;

    // SynthDefs

    SynthDef(\iannis_recorder_internal, {arg bufnum, in, isRun = 1;
      RecordBuf.ar(In.ar(in, 2), bufnum, 0, 1, 0, isRun, 0, doneAction: 2);
    }).add();

    SynthDef(\iannis_recorder_mic, {arg bufnum, in, isRun = 1;
      RecordBuf.ar(SoundIn.ar(in)!2, bufnum, 0, 1, 0, isRun, 0, doneAction: 2);
    }).add();

    SynthDef(\iannis_sample_player, {arg bufnum, fadeDur = 0.025, out = 0, isLoop = 0;
      var fades = EnvGen.ar(Env.circle([0, 1, 1, 0], [fadeDur, BufDur.kr(bufnum)-(fadeDur*3), fadeDur]));
      var output = PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum), loop: isLoop, doneAction: 2);
      output = output * fades;

      Out.ar(out, output);
    }).add();
  }

  maxDur_ {arg value;
    if (isRecording.not) {
      maxDur = value;

      recordingBuffer = Buffer.alloc(Server.default, value*Server.default.sampleRate, 2);
    } {
      "can't set maxDur while recording".warn;
    }
  }

  updateFilesList {
    fork{
      soundfilesInDir = SoundFile.collect(recordingDir+/+"*");

      Server.default.sync;

      delegate.didUpdateFilesList();
    }
  }

  recordingDir_ {arg value;
    fork {
      recordingDir = value;

      delegate.didUpdateDirectory();

      this.updateFilesList();

      Server.default.sync;

      if (soundfilesInDir.size > 0) {
        this.soundfile = soundfilesInDir[0];
      }
    }
  }

  writePlayerBuffer {
    fork {
      var numOfSamples, filePath;
      // init and copy data
      playerBuffer.free();
      numOfSamples = round(recordingDur * Server.default.sampleRate);
      playerBuffer = Buffer.alloc(Server.default, recordingDur*Server.default.sampleRate, 2);
      recordingBuffer.copyData(playerBuffer, numSamples: numOfSamples);

      Server.default.sync;

      // write a file
      filePath = recordingDir +/+ ("Recording-"++thisThread.seconds.asString).replace(".", "-") ++ ".aif";
      playerBuffer.write(filePath, "aiff", "float");

      Server.default.sync;

      // update soundfile
      // soundfile = SoundFile.new;
      soundfile.openRead(filePath);

      Server.default.sync;

      delegate.didUpdateSample();

      this.updateFilesList();
    }
  }

  soundfile_ {arg value;
    soundfile = value;
    fork {
      playerBuffer.free();
      playerBuffer = Buffer.read(Server.default, soundfile.path);

      Server.default.sync;

      delegate.didUpdateSample();
    }
  }

  record {
    if (isRecording.not) {
      Routine({
        var synthName;

        if (isPlaying) {this.stopSample()};
        
        "start recording".postln;

        isRecording = true;

        if (isInputHardware) {synthName = \iannis_recorder_mic} {synthName = \iannis_recorder_internal};

        recorderSynth = Synth.tail(nil, synthName, [
          \bufnum, recordingBuffer,
          \in, inputBusNum
        ]);
        startTime = TempoClock.default.seconds;

        delegate.didStartRecord();
      }).play(TempoClock.default, quant);
    }
  }

  stopRecording {
    if (isRecording) {
      Routine({
        "stop recording".postln;
        recorderSynth.set([\isRun, 0]);
        recordingDur = TempoClock.default.seconds - startTime;

        // init a player buffer and write a file
        this.writePlayerBuffer();

        isRecording = false;

        delegate.didStopRecord();
      }).play(TempoClock.default, quant);
    }
  }

  playSample {arg fadeDur = 0.025;
    if(isPlaying.not && isRecording.not) {
      Routine({
        var loop;
        if (isPlayingLoop) {loop = 1} {loop=0};

        playerSynth = Synth(\iannis_sample_player, [
          \bufnum, playerBuffer,
          \isLoop, loop,
          \fadeDur, fadeDur
        ]);

        isPlaying = true;

        playerSynth.onFree({
          isPlaying = false;
          delegate.didStopPlaySample();
          "stopped playing".postln;
        });

        delegate.didStartPlaySample();
      }).play(TempoClock.default, quant);
    }
  }

  stopSample {
    if (isPlaying && isRecording.not) {
      Routine({
        playerSynth.free();
      }).play(TempoClock.default, quant);
    }
  }

  stopSampleImmediately {
    if (isPlaying && isRecording.not) {
      playerSynth.free();
    }
  }

  isPlayingLoop_ {arg value;
    isPlayingLoop = value;
    if (playerSynth.isPlaying) {
      if (value) {
        playerSynth.set(\isLoop, 1);
      } {
        playerSynth.set(\isLoop, 0);
      }
    }
  }

}


