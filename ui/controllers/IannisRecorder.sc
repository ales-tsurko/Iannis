IannisRecorder : CompositeView {
  var playerSynth, recorderSynth,
  startTime, recordingDur,
  maxDur,
  recordingBuffer, playerBuffer,
  <filePath,
  <isPlaying,
  <isRecording,
  <>inputBusNum, <>quant,
  <recordingDir;

  *new {arg recordingDir;
    ^super.new.init(recordingDir);
  }

  init {arg samplesDir;
    recordingDir = samplesDir;
    inputBusNum = 0;
    quant = 4;
    isPlaying = false;
    isRecording = false;
    this.maxDur = 30;

    // SynthDefs

    SynthDef(\iannis_mic_in, {arg in, out;
      Out.ar(out, SoundIn.ar(in));
    }).add();

    SynthDef(\iannis_recorder, {arg bufnum, in, isRun = 1;
      RecordBuf.ar(In.ar(in, 2), bufnum, 0, 1, 0, isRun, 0, doneAction: 2);
    }).add();

    SynthDef(\iannis_sample_player, {arg bufnum, fadeDur = 0.025, out = 0, isLoop = 0;
      var fades = EnvGen.ar(Env.circle([0, 1, 1, 0], [fadeDur, BufDur.kr(bufnum)-(fadeDur*3), fadeDur]));
      var output = PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum), loop: isLoop);
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

  writePlayerBuffer {
    // init and copy data
    playerBuffer.free();
    playerBuffer = Buffer.alloc(Server.default, recordingDur*Server.default.sampleRate, 2);
    recordingBuffer.copyData(playerBuffer);

    // write a file
    filePath = recordingDir +/+ ("Recording-"++thisThread.seconds.asString).replace(".", "-") ++ ".aif";
    playerBuffer.write(filePath, "aiff", "float");
  }

  record {
    if (isRecording.not) {
      Routine({
        "start recording".postln;

        isRecording = true;

        recorderSynth = Synth.tail(nil, \iannis_recorder, [
          \bufnum, recordingBuffer,
          \in, inputBusNum
        ]);
        startTime = TempoClock.default.seconds;
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
      }).play(TempoClock.default, quant);
    }
  }

  playSample {arg isLoop = false, fadeDur = 0.025;
    if(isPlaying.not && isRecording.not) {
      Routine({
        var loop;
        if (isLoop) {loop = 1} {loop=0};
        playerSynth = Synth(\iannis_sample_player, [
          \bufnum, playerBuffer,
          \isLoop, loop,
          \fadeDur, fadeDur
        ]);

        isPlaying = isLoop;
      }).play(TempoClock.default, quant);
    }
  }

  stopSample {
    if (isPlaying && isRecording.not) {
      Routine({
        playerSynth.free();
        isPlaying = false;
      }).play(TempoClock.default, quant);
    }
  }

}
