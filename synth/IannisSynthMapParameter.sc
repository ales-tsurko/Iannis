IannisSynthMapParameter : CompositeView {
  var <key,
  <name,
  <parentSynthPage,
  <parameterBinder,
  <proxies,
  <proxiesGroup,
  nameLabel,
  parametersView,
  <textView,
  closeButton,
  evaluateButton,
  editButton,
  onOffButton,
  <isOn,
  xFadeNumberBox,
  xFadeLabel;
  
  *new {arg key, name, parentSynthPage;
    ^super.new.init(key, name, parentSynthPage);
  }

  init {arg aKey, aName, aDelegate;
    key = aKey;
    name = aName;
    parentSynthPage = aDelegate;
    parameterBinder = ();
    isOn = false;

    proxiesGroup = Group();
    // ignore CmdPeriod
    CmdPeriod.remove(proxiesGroup);

    // 127.do is faster than Array.fill
    proxies = [];
    127.do({
      var proxy = NodeProxy();
      proxies = proxies.add(proxy);
      proxy.group = proxiesGroup;

      // ignore CmdPeriod
      CmdPeriod.remove(proxy);
    });

    this.initNameLabel();
    this.initCloseButton();
    this.initEvaluateButton();
    this.initOnOffButton();
    this.initXFadeNumberBox();
    this.initXFadeLabel();
    this.initParametersView();
    this.initTextView();
    this.initEditButton();


    this.layout = VLayout(
      HLayout(
        closeButton, nameLabel, onOffButton, editButton, evaluateButton,
        nil,
        xFadeLabel, xFadeNumberBox
      ),
      parametersView,
      textView
    );
  }

  initNameLabel {
    nameLabel = StaticText();
    nameLabel.string = name;
  }

  initXFadeLabel {
    xFadeLabel = StaticText();
    xFadeLabel.string = "XFade Time (s):";
  }

  initCloseButton {
    closeButton = Button();
    closeButton.fixedWidth = 18;
    closeButton.fixedHeight = 18;
    closeButton.states = [["✖"]];

    closeButton.action = {arg but;
      if (but.value == 0) {
        this.showCloseAlert({
          var preset = this.parentSynthPage
          .parentSynthController
          .presetsManagerController
          .presetsManager
          .currentPreset;
          // get the source value
          var val = preset.values[key];

          // assign the source value
          this.parentSynthPage
          .parentSynthController
          .parameterBinder[key]
          .value(val);

          // clear the proxies
          this.proxiesGroup.free();
          this.proxies.do({arg np; np.free(0.1)});

          // update parameters list
          this.parentSynthPage.availableParameters = this.parentSynthPage.availableParameters.add(key);

          this.parentSynthPage.parametersListView.items = this.parentSynthPage.availableParameters;

          // remove key from the map of the current preset
          preset.map[key] = nil;

          this.close();
        });
      }
    };
  }

  initEditButton {
    editButton = Button();
    editButton.fixedWidth = 100;
    editButton.states = [["Edit"], ["Compact"]];

    editButton.action = {arg but;
      if (but.value == 0) {
        textView.visible = false;
        evaluateButton.visible = false;
        xFadeLabel.visible = false;
        xFadeNumberBox.visible = false;
      } {
        textView.visible = true;
        evaluateButton.visible = true;
        xFadeLabel.visible = true;
        xFadeNumberBox.visible = true;
      };
      // update preset value
      this.parentSynthPage
      .parentSynthController
      .presetsManagerController
      .presetsManager
      .currentPreset
      .setMapMode(
        this.key,
        but.value
      );
    };

    editButton.doAction();
  }

  initEvaluateButton {
    evaluateButton = Button();
    evaluateButton.fixedWidth = 100;
    evaluateButton.states = [["Evaluate"]];

    evaluateButton.action = {arg but;
      if (but.value == 0) {
        this.evaluate();
      } 
    };
  }

  initOnOffButton {
    onOffButton = Button();
    onOffButton.fixedWidth = 30;

    onOffButton.states = [["On"], ["Off"]];

    onOffButton.action = {arg but;
      var preset = this.parentSynthPage
      .parentSynthController
      .presetsManagerController
      .presetsManager
      .currentPreset;

      if (but.value == 1) {
        // off
        // set real/fixed value
        var val = preset.values[key];
        this.parentSynthPage.parentSynthController.node.set(key, val);

        isOn = false;
      } {
        // on
        isOn = true;
      };

      // update preset value
      preset.setMapState(
        this.key,
        but.value
      );

      // update selfvalue
      this.proxiesGroup.set(\selfvalue, preset.values[key]);
    };

    onOffButton.doAction();
  }

  initXFadeNumberBox {
    xFadeNumberBox = NumberBox();
    xFadeNumberBox.fixedWidth = 60;
    xFadeNumberBox.clipLo = 0.0;
    xFadeNumberBox.clipHi = 60;

    xFadeNumberBox.action = {arg num;
      this.proxies.do({arg np; np.fadeTime = num.value});
      
      // update preset value
      this.parentSynthPage
      .parentSynthController
      .presetsManagerController
      .presetsManager
      .currentPreset
      .setMapXFade(
        this.key,
        num.value
      );
    };
  }

  initParametersView {
    parametersView = CompositeView();
    parametersView.layout = VLayout();
  }

  initTextView {
    var preset = this.parentSynthPage
        .parentSynthController
        .presetsManagerController
        .presetsManager
        .currentPreset;
    var updatePresetFunc = {arg tv;
      tv.getValue({arg code;
        preset.setMapCode(
          this.key,
          code
        );
      });
    };
    textView = IannisAceWrapper();
    textView.fixedHeight = 170;

    textView.onLoadFinished = {arg wv;
      wv.setValue(
        "/*\n"
        "Ctrl-R to evaluate the entire document.\n"
        "Ctrl-` - switch between Vim/Normal mode.\n"
        "Ctrl-Alt-H - view all the keyboard shortcuts.\n"
        "*/\n"
        "\n"
        "'selfvalue'.kr;"
      );
    };

    textView.onEvaluate = {arg code;
      this.evaluateCodeAction(code);
      // update preset value
      updatePresetFunc.value(textView);
    };

    textView.onEvaluateSelection = {arg code;
      code.interpretPrint;
      // update preset value
      updatePresetFunc.value(textView);
    };

    textView.onHardStop = {
      ("hard stop").postln;
    };

    // update preset on focus changes
    textView.focusGainedAction = {arg tv;
      updatePresetFunc.value(tv);
    };
    textView.focusLostAction = {arg tv;
      updatePresetFunc.value(tv);
    };
  }

  evaluateCodeAction {arg code;
    var preset = this.parentSynthPage
    .parentSynthController
    .presetsManagerController
    .presetsManager
    .currentPreset;
    var compiled;

    // create UI
    this.parseCode(code);

    compiled = code.compile();

    // update NodeProxy
    this.proxies.do({arg np; 
      np.source = compiled;
      np.set(\selfvalue, preset.values[key]);
    });

    // update selfvalue
    this.proxiesGroup.set(\selfvalue, preset.values[key]);
  }

  evaluate {
    textView.getValue({arg codeString;
      this.evaluateCodeAction(codeString);
    });
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

    message.string = "This action is undoable. Are you sure you want to remove this block?";
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
    preset.map[this.key]!?{
      var code = preset.getMapCode(key)?"";
      var xfade = preset.getMapXFade(key)?0;
      var state = preset.getMapState(key)?0;
      var mode = preset.getMapMode(key)?0;

      xFadeNumberBox.valueAction = xfade;
      onOffButton.valueAction = state;
      editButton.valueAction = mode;
      textView.onLoadFinished = {arg tv;
        tv.setValue(code);
        this.evaluate();

        // it seems, this actually never used.
        // loading of the preset values done in
        // the parameter UI creation method (makeParameterView)
        this.loadPresetDataForUserDefinedUI(preset);

      };
    };
  }

  loadPresetDataForUserDefinedUI {arg preset;
    preset.getMapUIValues(key)!?{
      var obj = preset.getMapUIValues(key);

      obj.keysValuesDo({arg key, value;
        this.parameterBinder[key]!?{
          this.parameterBinder[key].value(value);
        };
      });
    };
  }

  // MIDI
  onNoteOn {arg noteNumber, velocity;
    var proxy = this.proxies[noteNumber];

    proxy.bus!?{
      if (isOn) {
        var voice = this.parentSynthPage
        .parentSynthController
        .node
        .midiVoices[noteNumber];

        // set map to the voice
        voice.set(key, proxy.bus.asMap);
      };

      // send API keys to the proxy
      proxy.set(\selfnote, noteNumber.midicps);
      proxy.set(\selfvelocity, velocity.linlin(0, 127, 0, 1));
      proxy.set(\selfgate, 1);
    }
  }

  onNoteOff {arg noteNumber;
    // send API keys to the proxy
    this.proxies[noteNumber].set(\selfgate, 0);
    this.proxies[noteNumber].set(\selfvelocity, 0);
  }
}