IannisRecorderController : CompositeView {
  var <recordingDir,
  <samplePath,
  <value,
  chooseDirectoryButton,
  filesListView,
  directoryLabel,
  maxDurationBox,
  sampleView,
  inputBusNumBox,
  isInHardwareCheckBox,
  recordButton,
  playButton,
  isLoopCheckBox,
  quantizeBox,
  <directoryWatcher,
  <recorder;

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
    chooseDirectoryButton.fixedWidth = 150;
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

    // delete file
    filesListView.keyDownAction = {arg view, char, mod, unicode, keycode, key;
      if (key == 0x01000003) {
        this.deleteFileAction(filesListView, filesListView.value);
      }
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
      directoryLabel,

      HLayout(
        filesListView, sampleView
      ),

      HLayout(
        VLayout(
          chooseDirectoryButton,
          nil
        ),

        VLayout(
          HLayout(
            recordButton, playButton, isLoopCheckBox,
            nil,
            quantizationLabel, quantizeBox
          ),
          HLayout(
            inputBusLabel, inputBusNumBox, isInHardwareCheckBox,
            nil,
            maxDurationLabel, maxDurationBox
          ),
          nil
        )
      )
    );

    // directory watcher
    directoryWatcher = IannisDirectoryWatcher(recordingDir, this);

    this.toFrontAction = {arg v;
      directoryWatcher.startWatch();
    };

    this.endFrontAction = {arg v;
      directoryWatcher.stopWatch();
    };
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
    this.stopPlayingSample();
  }

  deleteFileAction {arg view, index;
    samplePath!?{
      this.showDeleteFileAlert(samplePath.basename, index, {arg n;
        this.stopPlayingSample();

        recorder.deleteSoundFileAtIndex(n);

        if (recorder.soundfilesInDir.size > 0) {
          view.valueAction = (n-1).clip(0, view.items.size);
        } {
          samplePath = nil;
          view.items = [];
          nil;
        }
      });
    };
  }

  stopPlayingSample {
    if (recorder.isPlaying) {
      fork {
        recorder.stopSampleImmediately();

        Server.default.sync;

        AppClock.sched(0.0, {playButton.valueAction = 1});
      }
    }
  }

  samplePath_ {arg path;
    path!?{
      var isFileExists = File.existsCaseSensitive(path);

      if (isFileExists) {
        this.stopPlayingSample();

        samplePath = path;
        recorder.recordingDir = path.dirname;
      } {
        this.showSoundFileNotFoundAlert(path);
        // trig recordingDir of recorder to update directory listing
        path!?{
          recorder.recordingDir = path.dirname;
        };
      };
    }
  }

  showDeleteFileAlert {arg fileName, index, okCallback;
    var screenBounds = Window.screenBounds();
    var rect = Rect(
      screenBounds.width/2-125,
      screenBounds.height/2-50,
      250,
      100
    );
    var window = Window("Warning", rect, false);
    var message = StaticText();
    var okButton = Button();
    var cancelButton = Button();
    window.alwaysOnTop = true;

    message.string = "Are you sure you want to delete" + fileName++"?";
    message.align = \center;

    okButton.fixedWidth = 90;
    okButton.states = [["OK"]];
    okButton.action = {arg but;
      if (but.value == 0) {
        okCallback.value(index);
        window.close();
        this.directoryWatcher.startWatch();
      };
    };

    cancelButton.fixedWidth = 90;
    cancelButton.states = [["Cancel"]];
    cancelButton.action = {arg but;
      if (but.value == 0) {
        window.close();
        this.directoryWatcher.startWatch();
      };
    };

    window.layout = VLayout(
      message,
      HLayout(
        nil,
        cancelButton,
        okButton,
        nil
      )
    );

    window.front();
    this.directoryWatcher.stopWatch();
  }

  showSoundFileNotFoundAlert {arg path;
    var screenBounds = Window.screenBounds();
    var rect = Rect(
      screenBounds.width/2-150,
      screenBounds.height/2-100,
      300,
      200
    );
    var window = Window("Error", rect, false);
    var message = StaticText();
    var okButton = Button();
    window.alwaysOnTop = true;
    okButton.fixedWidth = 90;
    okButton.states = [["OK"]];
    okButton.action = {arg but;
      if (but.value == 0) {
        window.close();
      };
    };

    message.align = \center;

    message.string = path.basename+"not found at:\n"+path.dirname;

    window.layout = VLayout(
      message,
      HLayout(nil, okButton, nil)
    );
    window.front;
  }

  // delegate methods
  didUpdateSample {
    AppClock.sched(0.0, {
      sampleView.soundfile = recorder.soundfile;
      sampleView.read(0, recorder.soundfile.numFrames);
      sampleView.refresh();

      value = recorder.playerBuffer;
      samplePath = recorder.soundfile.path;

      this.doAction();
    });
  }

  didUpdateFilesList {
    AppClock.sched(0.0, {
      filesListView.clear();
      filesListView.items = [];
      recorder.soundfilesInDir.do({arg item; 
        filesListView.items = filesListView.items.add(item.path.basename);
      });

      // update selected index to current soundfile if the one exists
      this.samplePath!?{
        if ((File.existsCaseSensitive(this.samplePath)) && (this.samplePath.dirname == recorder.recordingDir)) {
          var index = filesListView.items.indexOfEqual(this.samplePath.basename);
          index??{index = 0};

          filesListView.valueAction = index;
        };
      };
    });
  }

  didUpdateDirectory {
    AppClock.sched(0.0, {
      directoryLabel.string = recorder.recordingDir;
      directoryWatcher.path = recorder.recordingDir;
    });
  }

  didChangeDirectoryContent {
    // trigger samplePath setter action
    // to restructure directory
    this.samplePath!?{
      this.samplePath = recorder.soundfile.path;
    };
  }

  didStartRecord {
    AppClock.sched(0.0, {
      playButton.value = 0;
      playButton.enabled = false;
      nil;
    });
  }

  didStopRecord {
    AppClock.sched(0.0, {
      playButton.enabled = true;
      nil;
    });
  }

  didStartPlaySample {
    AppClock.sched(0.0, {
      // code here
      nil;
    });
  }

  didStopPlaySample {
    AppClock.sched(0.0, {
      if (playButton.value == 1) {playButton.value = 0};
      nil;
    });
  }
}
