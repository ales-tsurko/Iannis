IannisMIDIManager {
  var <>map,
  <voicesManager,
  delegate, 
  <selectedDevice,
  <selectedDisconnectedDevice,
  <midiInputEnabled,
  <channel;

  *new {arg delegate;
    ^super.new.init(delegate);
  }

  init {arg aDelegate;
    delegate = aDelegate;
    midiInputEnabled = false;
    map = ();
    map[\cc] = ();
    channel = 0;
    voicesManager = IannisVoicesManager();

    this.initMIDIClient();
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

        this.map[\cc][key][\func].free();
        this.map[\cc][key] = nil;
      };

      // return non-nil value
      1;
    }??{
      // init new MIDI function for controller
      var newFunc = MIDIFunc.cc({arg val, num, chan, src;
        if ([src, num, chan] == this.map[\cc][key][\ccinfo]) {
          var spec = synthViewData[key][\spec];
          var value = spec.asSpec.map(val/127);
          AppClock.sched(0, {
            synthViewData[key][\updater].value(value);
          });
        };
      });

      // update data
      this.map[\cc][key] = ();
      this.map[\cc][key][\func] = newFunc;
      this.map[\cc][key][\ccinfo] = [sourceUID, ccNum, channel];
    }
  }

  removeMIDIController {arg key;
    this.map[\cc][key][\func].free();
    this.map[\cc][key] = nil;
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
      }
    };
  }
}
