IannisMIDIInManagerController : CompositeView {
  var <parentController,
  <midiSourcesMenu, 
  <channelNumberBox,
  <midiInputEnabled, 
  <midiManager,
  <panicButton;

  *new {arg parentController;
    ^super.new.init(parentController);
  }

  init {arg viewController;
    parentController = viewController;

    this.initPanicButton();
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
      nil,
      panicButton
    );

    this.deleteOnClose = false;
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
    midiManager.map[\learningfunc] = MIDIFunc.cc({arg val, num, chan, src;
      var key = this.parentController.selectedElementKey;

      key!?{
        this.midiManager.map[\cc][key]!?{
          if (this.midiManager.map[\cc][key][\ccinfo] != [src, num, chan]) {

          this.midiManager.map[\cc][key][\func].free();
          this.midiManager.map[\cc][key] = nil;
          };

          // return non-nil value
          1;
        }??{
          // init new MIDI function for controller
          var newFunc = MIDIFunc.cc({arg va, nu, cha, source;
            var spec = this.parentController.data[key][\spec];
            var view = this.parentController.data[key][\view];
            var value = spec.asSpec.map(va/127);
            AppClock.sched(0, {
              this.parentController.data[key][\updater].value(value);
            });
          }, num, chan, src);

          // update data
          this.midiManager.map[\cc][key] = ();
          this.midiManager.map[\cc][key][\func] = newFunc;
          this.midiManager.map[\cc][key][\ccinfo] = [src, num, chan];
        };
      };
    });
  }

  stopLearn {
    midiManager.map[\learningfunc].free();
    midiManager.map[\learningfunc] = nil;
  }
}
