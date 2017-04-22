IannisMIDIInManagerController : CompositeView {
  var <parentController,
  midiSourcesMenu, channelNumberBox,
  sourcesWatcher,
  <midiInputEnabled,
  midiChannel, selectedDevice, midiSourcesListSnaphot;

  *new {arg parentController;
    ^super.new.init(parentController);
  }

  init {arg viewController;
    parentController = viewController;

    this.initMIDISourcesMenu();
    this.initChannelNumberBox();
    this.initMIDIClient();

    midiSourcesMenu.valueAction = 0;
    midiInputEnabled = false;

    ~midiInLabel = StaticText();
    ~midiInLabel.string = "MIDI Input:";
    ~channelNumberLabel = StaticText();
    ~channelNumberLabel.string = "Ch. num.:";

    this.layout = HLayout(
      ~midiInLabel,
      midiSourcesMenu,
      ~channelNumberLabel,
      channelNumberBox,
      nil
    );

    this.onClose = {
      sourcesWatcher.stop();
    };
  }

  initMIDISourcesMenu {
    midiSourcesMenu = PopUpMenu();
    midiSourcesMenu.fixedWidth = 200;

    midiSourcesMenu.action = {arg popup;
      if (popup.value.notNil) {
        this.didSelectNewDevice(midiSourcesListSnaphot[popup.value-1]);
      }
    };
  }

  initChannelNumberBox {
    channelNumberBox = NumberBox();
    channelNumberBox.fixedWidth = 40;
    channelNumberBox.decimals = 0;
    channelNumberBox.clipLo = 0;
    channelNumberBox.clipHi = 16;

    channelNumberBox.action = {arg nb;
			this.didUpdateMIDIChannel(nb.value);
    };
  }

  initMIDIClient {
    if (MIDIClient.initialized.not) {
      MIDIClient.init();
    };

    midiSourcesListSnaphot = MIDIClient.sources;
    this.didUpdateMIDISources();

    // sources watcher
    (
      sourcesWatcher = Routine({
        loop {
          MIDIClient.list();

          if (MIDIClient.sources().size > 0) {
            if (midiSourcesListSnaphot.last.uid != MIDIClient.sources().last.uid || midiSourcesListSnaphot.size != MIDIClient.sources().size) {
              midiSourcesListSnaphot = MIDIClient.sources();
              this.didUpdateMIDISources();
            };
          };

          2.wait;
        }
      });

      SystemClock.play(sourcesWatcher);
    )
  }

  didUpdateMIDISources {
    var devicesNames = midiSourcesListSnaphot.collect(_.name);
    AppClock.sched(0, {
      midiSourcesMenu.items = devicesNames.insert(0, "None");
    });
  }

  didSelectNewDevice {arg device;
    if (device.isNil) {
      this.didDisableMIDIInput();
    } {
      midiInputEnabled = true;
      selectedDevice = device;
      ("selected device:"+selectedDevice.name).postln;
    }
  }

	didUpdateMIDIChannel {arg newChannel;
		midiChannel = newChannel;
	}

  didDisableMIDIInput {
    midiInputEnabled = false;
    "MIDI input is disabled".postln;
  }
}
