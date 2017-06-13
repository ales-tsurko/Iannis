IannisMIDIInManagerController : CompositeView {
  var <parentController,
  <midiSourcesMenu, 
  <channelNumberBox,
  <midiInputEnabled, 
  <midiManager,
  <panicButton,
  learningFunc;

  *new {arg parentController;
    ^super.new.init(parentController);
  }

  init {arg viewController;
    parentController = viewController;

    this.initPanicButton();
    this.initMIDISourcesMenu();
    this.initChannelNumberBox();

    midiManager = IannisMIDIManager(this);
    
    this.initLearningFunc();

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
      nil,
      panicButton
    );

    this.deleteOnClose = false;
  }

  initLearningFunc {
    learningFunc = MIDIFunc.cc({arg val, num, chan, src;
      var key = this.parentController.selectedElementKey;
      var data = this.parentController.data;

      key!?{
        this.midiManager.addMIDIControllerForParameter(key, src, num, chan, data);
      }
    });

    learningFunc.disable();
  }

  initPanicButton {
    panicButton = Button();
    panicButton.fixedWidth = 24;
    panicButton.fixedHeight = 24;
    panicButton.states = [["!"]];

    panicButton.action = {arg but;
      if (but.value == 0) {
        this.midiManager.reset();
      };
    };
  }

  initMIDISourcesMenu {
    midiSourcesMenu = PopUpMenu();
    midiSourcesMenu.fixedWidth = 100;

    midiSourcesMenu.action = {arg popup;
      if (popup.value.notNil) {
        this.midiManager.selectedDevice = IannisMIDIClient.sources[popup.value-1];
        this.midiManager.reset();
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
      this.midiManager.reset();
    };
  }

  cleanUp {
    this.midiManager.map[\noteOn].free();
    this.midiManager.map[\noteOff].free();
    this.midiManager.map[\bend].free();
    this.midiManager.map[\sustainPedal].free();
    this.midiManager.map[\cc].valuesDo({arg src;
      src[\func].free();
    });
    this.midiManager.reset();
  }
  
  startLearn {
    learningFunc.enable();
  }

  stopLearn {
    learningFunc.disable();
  }
}
