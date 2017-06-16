IannisMIDIManager {
  var <>map,
  <voicesManager,
  delegate, 
  parentViewController,
  <selectedDevice,
  <selectedDisconnectedDevice,
  <midiInputEnabled,
  <channel,
  learningFunc;

  *new {arg delegate, parentViewController;
    ^super.new.init(delegate, parentViewController);
  }

  init {arg aDelegate, parent;
    delegate = aDelegate;
    parentViewController = parent;
    midiInputEnabled = false;
    map = ();
    map[\cc] = ();
    channel = 0;
    voicesManager = IannisVoicesManager();

    this.initMIDIClient();
    this.initLearningFunc();
  }

  loadPreset {arg preset;
    var synthData = parentViewController
    .parentSynthController
    .data;

    preset.midiBindings!?{
      preset.midiBindings.keysValuesDo({arg key, value;
        this.addMIDIControllerForParameter(
          key, 
          value[0],
          value[1], 
          value[2],
          synthData
        );
      });
    };
  }

  initMIDIClient {
    IannisMIDIClient.addOnUpdateSourcesAction({delegate.didUpdateMIDISources()});

    if (IannisMIDIClient.initialized) {
      IannisMIDIClient.list;
      delegate.didUpdateMIDISources();
    } {
      IannisMIDIClient.init();
    };
  }

  initLearningFunc {
    learningFunc = MIDIFunc.cc({arg val, num, chan, src;
      var key = parentViewController.parentSynthController.selectedElementKey;
      var data = parentViewController.parentSynthController.data;

      key!?{
        this.addMIDIControllerForParameter(key, src, num, chan+1, data);
      }
    });

    learningFunc.disable();
  }

  selectedDevice_ {arg device;
    device!?{selectedDevice = device};

    // selectedDevice = device;
    this.midiInputEnabled = device.notNil;

    delegate.didSelectNewDevice(device);
  }

  selectedDeviceIndex {
    var index;

    selectedDevice!?{
      index = IannisMIDIClient.sources.detectIndex({arg d;
        d.uid == selectedDevice.uid;
      });
    };

    index!?{^(index + 1)}??{^0};
  }

  midiInputEnabled_ {arg newValue;
    midiInputEnabled = newValue;

    if (midiInputEnabled.not) {
      this.reset();
      delegate.didDisableMIDIInput();
    }
  }

  channel_ {arg newValue;
    channel = newValue;
    delegate.didUpdateMIDIChannel(channel);
  }

  reset {
    this.voicesManager.releaseAll();
  }

  addMIDIControllerForParameter {arg key, sourceUID, ccNum, channel, synthViewData;
    this.map[\cc][key]!?{
      if (this.map[\cc][key][\ccinfo] != [sourceUID, ccNum, channel]) {
        this.updateMIDIControllerParameters(key, sourceUID, ccNum, channel);
      };

      // return non-nil value
      1;
    }??{
      // init new MIDI function for controller
      var newFunc = MIDIFunc.cc({arg val, num, chan, src;
        var ccInfo = this.map[\cc][key][\ccinfo];
        var mapSrc = ccInfo[0];
        var mapNum = ccInfo[1];
        var mapChan = ccInfo[2];

        if ([src, num] == [mapSrc, mapNum]) {
          if ((mapChan == 0) || (mapChan == (chan+1))) {
            var spec = synthViewData[key][\spec];
            var value = spec.asSpec.map(val/127);
            AppClock.sched(0, {
              synthViewData[key][\updater].value(value);
            });
          };
        };
      });

      // update data
      this.map[\cc][key] = ();
      this.map[\cc][key][\func] = newFunc;
      this.map[\cc][key][\ccinfo] = [sourceUID, ccNum, channel];

      // call delegate method
      delegate.didAddMIDIControllerToMap(key, sourceUID, ccNum, channel);
    }
  }

  removeMIDIController {arg key;
    this.map[\cc][key][\func].free();
    this.map[\cc][key] = nil;

    delegate.didRemoveMIDIControllerFromMap(key);
  }

  updateMIDIControllerParameters {arg key, sourceUID, ccNum, channel;
    this.map[\cc][key]!?{
      sourceUID!?{
        this.map[\cc][key][\ccinfo][0] = sourceUID;
      };

      ccNum!?{
        this.map[\cc][key][\ccinfo][1] = ccNum;
      };

      channel!?{
        this.map[\cc][key][\ccinfo][2] = channel;
      };

      delegate.didUpdateMIDIControllerInMap(
        key, 
        this.map[\cc][key][\ccinfo][0],
        this.map[\cc][key][\ccinfo][1],
        this.map[\cc][key][\ccinfo][2]
      );
    };
  }

  startLearn {
    learningFunc.enable();
  }

  stopLearn {
    learningFunc.disable();
  }

}
