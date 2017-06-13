IannisSynthMIDIViewController : CompositeView {
  var <parentController,
  <midiSourcesMenu, 
  <channelNumberBox,
  <midiInputEnabled, 
  <midiManager,
  <panicButton,
  parameters;

  *new {arg parentController;
    ^super.new.init(parentController);
  }

  init {arg viewController;
    parentController = viewController;

    this.initPanicButton();
    this.initMIDISourcesMenu();
    this.initChannelNumberBox();

    midiManager = IannisMIDIManager(this, this);
    

    midiSourcesMenu.valueAction = 0;

    this.initParameters();
    
    ~midiInLabel = StaticText();
    ~midiInLabel.string = "Keyboard Input:";
    ~channelNumberLabel = StaticText();
    ~channelNumberLabel.string = "Keyboard Channel:";

    this.layout = VLayout(
      HLayout(
        ~midiInLabel,
        midiSourcesMenu,
        ~channelNumberLabel,
        channelNumberBox,
        nil,
        panicButton
      ),
      nil
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

  initParameters {
    parameters = ();

    midiManager.map[\cc].keysValuesDo({arg key, obj;
      this.addParameter(
        key, 
        obj[\ccinfo][0], 
        obj[\ccinfo][1], 
        obj[\ccinfo][2]
      );
    });
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

  addParameter {arg key, sourceUID, ccNum, channel;
    AppClock.sched(0, {
      var view = IannisMIDIParameterView(
        this,
        key,
        sourceUID,
        ccNum,
        channel
      );

      parameters[key] = view;

      this.layout.insert(view, this.layout.children.size);
    });
  }

  removeParameter {arg key;
    AppClock.sched(0, {
      this.midiManager.removeMIDIController(key);
      parameters[key].close();
      parameters[key] = nil;
    });
  }
}




IannisMIDIParameterView : CompositeView {
  var <parent,
  <parameterKey,
  <sourceUID,
  <ccNum,
  <channel,
  closeButton,
  parameterLabel,
  sourcesPopup, 
  sourcesLabel,
  ccNumPopup, 
  ccLabel,
  channelNumberBox, 
  channelLabel;

  *new {arg parent, parameterKey, sourceUID, ccNum, channel;
    ^super.new.init(parent, parameterKey, sourceUID, ccNum, channel);
  }

  init {arg aParent, key, src, cc, chan;
    parent = aParent;
    parameterKey = key;
    sourceUID = src;
    ccNum = cc;
    channel = chan;

    this.initCloseButton();
    this.initParameterLabel();
    this.initSourcesPopup();
    this.initCCNumPopup();
    this.initChannelNumberBox();

    this.layout = HLayout(
      closeButton,
      parameterLabel,
      nil,
      sourcesLabel,
      sourcesPopup,
      nil,
      ccLabel,
      ccNumPopup,
      nil,
      channelLabel,
      channelNumberBox
    );
  }

  initCloseButton {
    closeButton = Button();
    closeButton.fixedWidth = 18;
    closeButton.fixedHeight = 18;
    closeButton.states = [["✖"]];

    closeButton.action = {arg but;
      if (but.value == 0) {
        this.showCloseAlert({
          parent.removeParameter(this.parameterKey);
        });
      }
    };
  }

  initParameterLabel {
    parameterLabel = StaticText();
    parameterLabel.string = parameterKey.asString;
  }

  initSourcesPopup {
    var sources = IannisMIDIClient.sources;
    var names = sources.collect(_.name);
    sourcesLabel = StaticText();
    sourcesLabel.string = "Input:";
    sourcesPopup = PopUpMenu();
    sourcesPopup.items = names;

    // FIXME:
    // конфигурация подключеных устройств может измениться,
    // что должно отражаться в меню доступных устройств,
    // иначе при изменении конфигурации, пользователь
    // будет работать с некорректными данными (то есть это
    // потенциальный баг)

    sourcesPopup.action = {arg popup;
      sourceUID = sources[popup.value].uid;
      // change the source
      parent.midiManager
      .updateMIDIControllerParameters(this.parameterKey, sourceUID);
    };

    // init value
    sourcesPopup.value = IannisMIDIClient.sources.detectIndex({arg d;
      d.uid == this.sourceUID;
    });
  }

  initCCNumPopup {
    var ccs = (0..127);
    ccLabel = StaticText();
    ccLabel.string = "CC Num.:";
    ccNumPopup = PopUpMenu();
    ccNumPopup.items = ccs;

    ccNumPopup.action = {arg popup;
      ccNum = ccs[popup.value];

      // change the CC number
      parent.midiManager
      .updateMIDIControllerParameters(this.parameterKey, ccNum: ccNum);
    };

    ccNumPopup.value = this.ccNum;
  }

  initChannelNumberBox {
    channelLabel = StaticText();
    channelLabel.string = "Channel:";

    channelNumberBox = NumberBox();
    channelNumberBox.fixedWidth = 60;
    channelNumberBox.clipLo = 0;
    channelNumberBox.clipHi = 16;

    channelNumberBox.action = {arg num;
      channel = num.value;

      // change the channel
      parent.midiManager
      .updateMIDIControllerParameters(this.parameterKey, channel: channel);
    };

    channelNumberBox.value = this.channel;
  }

  showCloseAlert {arg okCallback;
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

    message.string = "Are you really want to remove this MIDI binding?";
    message.align = \center;

    okButton.fixedWidth = 90;
    okButton.states = [["OK"]];
    okButton.action = {arg but;
      if (but.value == 0) {
        okCallback.value();
        window.close();
      };
    };

    cancelButton.fixedWidth = 90;
    cancelButton.states = [["Cancel"]];
    cancelButton.action = {arg but;
      if (but.value == 0) {
        window.close();
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
  }

  sourceUID_ {arg newValue;
    sourceUID = newValue;
    AppClock.sched(0, {
      sourcesPopup.value = IannisMIDIClient.sources.detectIndex({arg d;
        d.uid == newValue;
      });
    });
  }

  ccNum_ {arg newValue;
    ccNum = newValue;
    AppClock.sched(0, {
      ccNumPopup.value = newValue;
    });
  }

  channel_ {arg newValue;
    channel = newValue;
    AppClock.sched(0, {
      channelNumberBox.value = newValue;
    });
  }
}
