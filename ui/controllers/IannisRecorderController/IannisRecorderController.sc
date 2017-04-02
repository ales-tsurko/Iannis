IannisRecorderController : CompositeView {
  var <recordingDir,
  chooseDirectoryButton,
  filesListView,
  directoryLabel,
  maxDurationBox,
  sampleView,
  inputBusNumBox, isInHardwareCheckBox,
  recordButton, playButton,
  isLoopCheckBox,
  quantizeBox,
  recorder;

  *new {arg dir;
    ^super.new.init(dir);
  }

  init {arg samplesDir;
    var inputBusLabel = StaticText.new;
    var quantizationLabel = StaticText.new;
    var maxDurationLabel = StaticText.new;
    directoryLabel = StaticText.new;
    directoryLabel.align = \left;
    recordingDir = samplesDir;
    recorder = IannisRecorder(recordingDir, this);

    // choose directory
    chooseDirectoryButton = Button.new;
    chooseDirectoryButton.fixedWidth = 100;
    chooseDirectoryButton.states = [["Directory"]];
    chooseDirectoryButton.action = {arg button;
      this.chooseDirectoryButtonAction(button);
    };

    // max duration
    maxDurationLabel.string = "Max. rec. dur.:";
    maxDurationBox = NumberBox.new;
    maxDurationBox.fixedWidth = 50;
    maxDurationBox.clipLo = 0.1;
    maxDurationBox.clipHi = 60*20; // 20 minutes
    maxDurationBox.action = {arg box;
      this.maxDurationBoxAction(box);
    };

    maxDurationBox.valueAction = 30;

    // input number
    inputBusLabel.string = "Input:";
    inputBusNumBox = NumberBox.new;
    inputBusNumBox.fixedWidth = 50;
    inputBusNumBox.decimals = 0;
    inputBusNumBox.clipLo = 0;
    inputBusNumBox.clipHi = Server.default.options.numAudioBusChannels;
    inputBusNumBox.action = {arg box;
      this.inputBusNumBoxAction(box);
    };
    inputBusNumBox.valueAction = 0;

    // is input hardware
    isInHardwareCheckBox = CheckBox.new;
    isInHardwareCheckBox.string = "hardware";
    isInHardwareCheckBox.action = {arg checkBox;
      this.isInHardwareCheckBoxAction(checkBox);
    };

    // sample view
    sampleView = SoundFileView.new;
    // sampleView.gridOn = true;
    // sampleView.gridResolution = TempoClock.default.tempo.reciprocal;

    // record button
    recordButton = Button.new;
    recordButton.fixedWidth = 100;
    recordButton.states = [["Record"], ["Stop"]];
    recordButton.action = {arg button;
      this.recordButtonAction(button);
    };

    // files list
    filesListView = ListView.new;
    filesListView.fixedWidth = 150;
    filesListView.selectionMode = \single;
    filesListView.action = {arg listView;
      this.filesListViewAction(listView);
    };

    // play button
    playButton = Button.new;
    playButton.fixedWidth = 100;
    playButton.states = [["Play"], ["Stop"]];
    playButton.action = {arg button;
      this.playButtonAction(button);
    };

    // is loop
    isLoopCheckBox = CheckBox.new;
    isLoopCheckBox.string = "loop";
    isLoopCheckBox.action = {arg checkBox;
      this.isLoopCheckBoxAction(checkBox);
    };

    // quantize
    quantizationLabel.string = "Quantization:";
    quantizeBox = NumberBox.new;
    quantizeBox.fixedWidth = 50;
    quantizeBox.clipLo = 0;
    quantizeBox.minDecimals = 0;
    quantizeBox.maxDecimals = 4;
    quantizeBox.alt_scale = 0.25;
    quantizeBox.shift_scale = 1;

    quantizeBox.action = {arg box;
      this.quantizeBoxAction(box);
    };

    quantizeBox.valueAction = 1;

    this.layout = VLayout(
      HLayout(
        chooseDirectoryButton,
        nil,
        inputBusLabel, inputBusNumBox, isInHardwareCheckBox,
        nil,
        maxDurationLabel, maxDurationBox, 
      ),

      directoryLabel, 
      
      HLayout(
        filesListView, sampleView 
      ),
      
      HLayout(
        recordButton, playButton, isLoopCheckBox,
        nil,
        quantizationLabel, quantizeBox
      )
    )
  }

  chooseDirectoryButtonAction {arg button;
    if (button.value == 0) {
      FileDialog({arg path;
          recorder.recordingDir = path;
        }, {}, 2, 0, true);
    }
  }

  maxDurationBoxAction {arg box;
    recorder.maxDur = box.value;
  }

  inputBusNumBoxAction {arg box;
    recorder.inputBusNum = box.value;
  }

  quantizeBoxAction {arg box;
    recorder.quant = box.value;
  }

  isInHardwareCheckBoxAction {arg checkBox;
    recorder.isInputHardware = checkBox.value;
  }

  recordButtonAction {arg button;
    if (button.value == 1) {
      recorder.record();
    } {
      recorder.stopRecording();
    }
  }

  playButtonAction {arg button;
    if (button.value == 1) {
      recorder.playSample();
    } {
      recorder.stopSample();
    }
  }

  isLoopCheckBoxAction {arg checkBox;
    recorder.isPlayingLoop = checkBox.value;
  }

  filesListViewAction {arg listView;
    recorder.soundfile = recorder.soundfilesInDir[listView.value];
    if (recorder.isPlaying) {
      fork {
        recorder.stopSampleImmediately();

        Server.default.sync;

        AppClock.sched(0.0, {playButton.valueAction = 1});
      }
    }
  }

  // delegate methods
  didUpdateSample {
    AppClock.sched(0.0, {
      sampleView.soundfile = recorder.soundfile;
      sampleView.read(0, recorder.soundfile.numFrames);
      sampleView.refresh();
    });
  }

  didUpdateFilesList {
    AppClock.sched(0.0, {
      filesListView.clear();
      filesListView.items = [];
      recorder.soundfilesInDir.do({arg item; 
        filesListView.items = filesListView.items.add(item.path.basename);
      });
    });
  }

  didUpdateDirectory {
    AppClock.sched(0.0, {
      directoryLabel.string = recorder.recordingDir;
    });
  }

  didStartRecord {
    AppClock.sched(0.0, {
      playButton.value = 0;
      playButton.enabled = false;
    });
  }

  didStopRecord {
    AppClock.sched(0.0, {
      playButton.enabled = true;
    });
  }

  didStartPlaySample {
    AppClock.sched(0.0, {
      // code here
    });
  }

  didStopPlaySample {
    AppClock.sched(0.0, {
      if (playButton.value == 1) {playButton.value = 0};
    });
  }
}
