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

      // update preset
      this.parentSynthController
      .presetsManagerController
      .presetsManager
      .currentPreset
      .setPolyphony(nb.value);
    };
  }

  initMonophonicModePopup {
    monophonicModePopup = PopUpMenu();
    monophonicModePopup.items = ["Normal", "Legato"];

    monophonicModePopup.action = {arg pp;
      var value = [\normal, \legato][pp.value];
      this.midiManager
      .voicesManager
      .monophonicMode = value;

      // update preset
      this.parentSynthController
      .presetsManagerController
      .presetsManager
      .currentPreset
      .setMonophonicMode(value);
    };
  }

  initMIDISourcesMenu {
    midiSourcesMenu = PopUpMenu();
    midiSourcesMenu.fixedWidth = 100;

    midiSourcesMenu.action = {arg popup;
      popup.value!?{
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

  removeParameter {arg key, showAlert = true;
    parameters[key]!?{
      var callBack = {
        AppClock.sched(0, {
          this.midiManager.removeMIDIController(key);
          parameters[key].close();
          parameters[key] = nil;
        });
      };

      if (showAlert) {
        this.showCloseAlert({callBack.value()}, key);
      } {
        callBack.value();
      };
    }
  }

  setAvailable {arg key, value;
    parameters[key]!?{
      AppClock.sched(0, {
        parameters[key].avalaible = value;
      });
    }
  }

  // superclass method
  addParameterForKey {arg key;
    this.midiManager.addMIDIControllerForParameter(
      key, 
      0, 
      1,
      0, 
      this.parentSynthController.data
    );
  }

  fetchAvailableParameters {arg preset;
    this.midiManager!?{
      availableParameters = this.parentSynthController.data.keys.asArray;

      // make the parameter unavalaible if it's presented in the map
      // TODO: do it more efficiently
      this.midiManager.map[\cc].keysDo({arg key;
        var index = availableParameters.indexOf(key);
        index!?{availableParameters.removeAt(index)};
      });
    }??{
      availableParameters = [];
    }
    // preset!?{
      // availableParameters = this.midiManager.map[\cc].keys.asArray;
      // preset.midiBindings!?{
        // preset.midiBindings.keysDo({arg key;
          // // make the parameter unavalaible if it's presented in the map
          // availableParameters.removeAt(
            // availableParameters.indexOf(key)
          // );
        // });
      // };
    // }??{
      // availableParameters = [];
    // };
  }

  showCloseAlert {arg okCallback, key;
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

    message.string = "Are you really want to remove MIDI binding for '"++key++"'?";
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

  onLoadPreset {arg preset;
    preset!?{
      // clean up view
      parameters.keysDo({arg key;
        this.removeParameter(key, false);
      });

      // load preset data
      this.midiManager.loadPreset(preset);

      // update view for polyphony
      polyphonyNumberBox
      .valueAction = preset.getPolyphony()?16;

      monophonicModePopup
      .valueAction = if (\normal == (preset.getMonophonicMode()?\legato)) {0} {1};

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
  <sourcesPopup, 
  sourcesLabel,
  ccNumPopup, 
  ccLabel,
  channelNumberBox, 
  channelLabel,
  <avalaible = true;

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
        parent.removeParameter(this.parameterKey);
      }
    };
  }

  initParameterLabel {
    parameterLabel = StaticText();
    parameterLabel.string = parameterKey.asString;
  }

  initSourcesPopup {
    var names = IannisMIDIClient.sources.collect(_.name);
    var defaultIndex;
    names = names.insert(0, "None");
    sourcesLabel = StaticText();
    sourcesLabel.string = "Input:";
    sourcesPopup = PopUpMenu();
    sourcesPopup.items = names;

    sourcesPopup.action = {arg popup;
      popup.value!?{
        var value = IannisMIDIClient.sources[popup.value-1];
        sourceUID = value!?{value.uid}??{0};

        // change the source
        parent.midiManager
        .updateMIDIControllerParameters(this.parameterKey, sourceUID);
      };
    };

    // init value
    defaultIndex = IannisMIDIClient.sources.detectIndex({arg d;
      d.uid == this.sourceUID;
    });
    sourcesPopup.valueAction = defaultIndex!?{defaultIndex+1}??{0};
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

  sourceUID_ {arg newValue;
    sourceUID = newValue;
    AppClock.sched(0, {
      var index = IannisMIDIClient.sources.detectIndex({arg d;
        d.uid == newValue;
      });
      sourcesPopup.value = index!?{index+1}??{0};
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

  avalaible_ {arg newValue;
    avalaible = newValue;

    if (avalaible) {
      parameterLabel.stringColor = Color.black();
    } {
      parameterLabel.stringColor = Color.red();
    };
  }
}
