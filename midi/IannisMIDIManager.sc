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
}
