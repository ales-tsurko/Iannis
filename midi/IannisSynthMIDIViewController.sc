IannisSynthMIDIViewController : IannisSynthMapPage {
  var monophonicModePopup,
  polyphonyNumberBox,
  <midiSourcesMenu, 
  <channelNumberBox,
  <midiInputEnabled, 
  <midiManager;

  init {arg viewController;
    var polyphonyLabel, monophonicModeLabel,
    midiInLabel, channelNumberLabel;

    super.init(viewController);

    this.initPolyphonyNumberBox();
    this.initMonophonicModePopup();
    this.initMIDISourcesMenu();
    this.initChannelNumberBox();

    midiManager = IannisMIDIManager(this, this);
    

    midiSourcesMenu.valueAction = 0;

    this.initParameters();
    
    // labels
    polyphonyLabel = StaticText();
    monophonicModeLabel = StaticText();
    midiInLabel = StaticText();
    channelNumberLabel = StaticText();
    polyphonyLabel.string = "Polyphony:";
    monophonicModeLabel.string = "Monophonic mode:";
    midiInLabel.string = "Keyboard Input:";
    channelNumberLabel.string = "Keyboard Channel:";

    this.layout.insert(
      HLayout(
        polyphonyLabel,
        polyphonyNumberBox,
        monophonicModeLabel,
        monophonicModePopup,
        nil
      )
    );

    this.layout.insert(
      HLayout(
        midiInLabel,
        midiSourcesMenu,
        channelNumberLabel,
        channelNumberBox,
        nil
      )
    );

    this.layout.add(
      nil
    );
  }

  initPolyphonyNumberBox {
    polyphonyNumberBox = NumberBox();
    polyphonyNumberBox.fixedWidth = 48;
    polyphonyNumberBox.decimals = 0;
    polyphonyNumberBox.clipLo = 1;
    polyphonyNumberBox.clipHi = 128;

    polyphonyNumberBox.action = {arg nb;
      this.midiManager
      .voicesManager
      .allowedNumberOfVoices = nb.value;
    };
  }

  initMonophonicModePopup {
    monophonicModePopup = PopUpMenu();
    monophonicModePopup.items = ["Normal", "Legato"];

    monophonicModePopup.action = {arg pp;
      this.midiManager
      .voicesManager
      .monophonicMode = [\normal, \legato][pp.value];
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

  // called by delegate
  // don't call directly, use midiManager.addMIDIControllerForParameter
  // instead
  addParameter {arg key, sourceUID, ccNum, channel;
    AppClock.sched(0, {
      var view = IannisMIDIParameterView(
        this,
        key,
        sourceUID?0,
        ccNum?1,
        channel?0
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

  // superclass method
  addParameterForKey {arg key;
    var synthData = this.parentSynthController.data;
    this.midiManager.addMIDIControllerForParameter(
      key, 
      0, 
      1,
      0, 
      synthData
    );
  }

  fetchAvailableParameters {arg preset;
    // availableParameters = this.midiManager
    preset!?{
      availableParameters = preset.values.keys.asArray;
      preset.midiBindings!?{
        preset.midiBindings.keysDo({arg key;
          // make the parameter unavalaible if it's presented in the map
          availableParameters.removeAt(
            availableParameters.indexOf(key)
          );
        });
      };
    }??{
      availableParameters = [];
    };
  }

  onLoadPreset {arg preset;
    preset!?{
      // clean up view
      parameters.keysDo({arg key;
        this.removeParameter(key);
      });

      // load preset data
      this.midiManager.loadPreset(preset);

      // fetch available parameters
      this.fetchAvailableParameters(preset);
    }
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
    })?0;
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
