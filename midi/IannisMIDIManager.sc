IannisMIDIManager {
  var <>map, delegate, 
  <selectedDevice,
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
    selectedDevice = device;
    this.midiInputEnabled = device.notNil;

    delegate.didSelectNewDevice(selectedDevice);
  }
  
  midiInputEnabled_ {arg newValue;
    midiInputEnabled = newValue;

    if (midiInputEnabled.not) {
      delegate.didDisableMIDIInput();
    }
  }

  channel_ {arg newValue;
    channel = newValue;
    delegate.didUpdateMIDIChannel(channel);
  }

  reset {
    delegate.parentController.node.releaseMIDIVoices();
  }
}
