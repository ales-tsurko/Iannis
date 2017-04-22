IannisMIDIInManagerController : CompositeView {
  var <parentController,
  <midiSourcesMenu, <channelNumberBox,
  <midiInputEnabled, <midiManager;

  *new {arg parentController;
    ^super.new.init(parentController);
  }

  init {arg viewController;
    parentController = viewController;

    this.initMIDISourcesMenu();
    this.initChannelNumberBox();
    midiManager = IannisMIDIManager(this);

    midiSourcesMenu.valueAction = 0;

    ~midiInLabel = StaticText();
    ~midiInLabel.string = "MIDI Input:";
    ~channelNumberLabel = StaticText();
    ~channelNumberLabel.string = "Ch. num.:";

    this.layout = HLayout(
      // ~midiInLabel,
      midiSourcesMenu,
      ~channelNumberLabel,
      channelNumberBox,
      nil
    );
  }

  initMIDISourcesMenu {
    midiSourcesMenu = PopUpMenu();
    midiSourcesMenu.fixedWidth = 100;

    midiSourcesMenu.action = {arg popup;
      if (popup.value.notNil) {
        this.midiManager.selectedDevice = IannisMIDIClient.sources[popup.value-1];
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
			this.midiManager.channel = nb.value;
    };
  }
}
