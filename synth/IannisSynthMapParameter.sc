IannisSynthMapParameter : CompositeView {
  var <key,
  <name,
  <parentSynthPage,
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
    isOn = false;

    proxiesGroup = Group();

    // 127.do is faster than Array.fill
    proxies = [];
    127.do({
      var proxy = NodeProxy();
      proxies = proxies.add(proxy);
      proxy.group = proxiesGroup;
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
          var parentSynthController = this.parentSynthPage
          .parentSynthController;
          var preset = parentSynthController
          .presetsManagerController
          .presetsManager
          .currentPreset;
          // get the source value
          var val = preset.values[key];

          // assign the source value
          parentSynthController
          .data[key][\updater]
          .value(val);

          // clear the proxies
          this.proxiesGroup.free();
          this.proxies.do({arg np; np.free(0.1)});

          // update parameters list
          this.parentSynthPage.availableParameters = this.parentSynthPage.availableParameters.add(key);

          this.parentSynthPage.parametersListView.items = this.parentSynthPage.availableParameters;

          // remove key from the map of the current preset
          preset.map[key] = nil;

          parentSynthPage.willCloseParameter(key);
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
        var dataKey = (this.key++'.'++key).asSymbol;
        var parentSynthController = this.parentSynthPage
        .parentSynthController;

        parentSynthController
        .data[dataKey]!?{
          this.parentSynthPage
          .parentSynthController
          .data[dataKey][\updater].value(value);
        };
      });
    };
  }
  
  didFinishParsing {
    // var parentSynthController = this.parentSynthPage
    // .parentSynthController;
    // var preset = parentSynthController
    // .presetsManagerController
    // .presetsManager
    // .currentPreset;
    // parentSynthController.midiView.onLoadPreset(preset);
    // preset.midiBindings().postln;
  }

  // MIDI
  onNoteOn {arg noteNumber, velocity;
    var proxy = this.proxies[noteNumber];

    proxy.bus!?{
      if (isOn) {
        var voice = this.parentSynthPage
        .parentSynthController
        .midiView
        .midiManager
        .voicesManager
        .getVoice(noteNumber);

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


/*
* Parser
*/

+ IannisSynthMapParameter {
  
  parseCode {arg codeStr;
    var comments = codeStr.findRegexp("/\\*(.|[\\r\\n])*?\\*/");

    // reinit the parameters view
    parametersView.removeAll;

    // parse comments
    comments.do({arg item;
      var comment = item[1];
      if (comment.size > 0) {
        this.parseComment(comment);
      }
    });

    this.didFinishParsing();
  }

  parseComment {arg commentStr;
    var rows = commentStr.findRegexp("<\\s*prow\\s*>\\s*[\\n\\r]+[\\w\\s\\.:]+[\\n\\r]*");

    rows.do({arg item;
      var row = item[1];

      if (row.size > 0) {
        var parameterRowView = this.parseParametersRow(row);
        parametersView.layout.add(parameterRowView);
      }
    });
  }

  parseParametersRow {arg rowStr;
    var parameterRowView = this.makeParameterRowView();
    var parameters = rowStr.split($\n);
    parameters = parameters.drop(1);

    parameters.do({arg parameter;
      if (parameter.size > 0) {
       var parameterView = this.parseParameter(parameter);
       parameterRowView.canvas
       .layout
       .insert(
         parameterView, 
         parameterRowView.canvas.layout.children.size
       );
      }
    });

    ^parameterRowView;
  }

  parseParameter {arg parameterStr;
    var view;
    var nameAndParamSplit = parameterStr.split($:);
    var fillData = {arg key, view, spec;
      var dataKey = (this.key++'.'++key).asSymbol;
      var parentSynthController = this.parentSynthPage
      .parentSynthController;
      parentSynthController.data[dataKey]??{parentSynthController.data[dataKey] = ()};
      parentSynthController.data[dataKey][\view] = view;
      parentSynthController.data[dataKey][\spec] = spec;
    };

    case
    // the parameter has name
    {nameAndParamSplit.size == 2} {
      var name = nameAndParamSplit[0];
      var key = nameAndParamSplit[1].findRegexp("\\w+")[0][1].asSymbol;
      var spec = this.parseSpec(nameAndParamSplit[1]);
      view = this.makeParameterView(name, key, spec);
      fillData.value(key, view, spec);
    }
    // the parameter has no name
    {nameAndParamSplit.size == 1} {
      var key = nameAndParamSplit[0].findRegexp("\\w+")[0][1].asSymbol;
      var spec = this.parseSpec(nameAndParamSplit[0]);
      view = this.makeParameterView(nil, key, spec);
      fillData.value(key, view, spec);
    }
    // else
    {true} {
      ("Parse parameter:\n\t"+parameterStr).error;
    };

    ^view;
  }

  parseSpec {arg parameterStr;
    var tokens = parameterStr.findRegexp("[\\w\\.]+");
    var spec = ControlSpec();

    tokens.do({arg item, n;
      var value = item[1];

      switch(n,
        // minval
        1, {
          spec.minval = value.asFloat;
        },
        // maxval
        2, {
          spec.maxval = value.asFloat;
        },
        // warp
        3, {
          if ("[a-zA-Z]".matchRegexp(value)) {
            spec.warp = value.asSymbol;
          } {
            spec.warp = value.asFloat;
          };
        },
        // step
        4, {
          spec.step = value.asFloat;
        },
        // default value
        5, {
          spec.default = value.asFloat;
        },
        // units
        6, {
          spec.units = value;
        }
      );
    });

    ^spec;
  }

  makeParameterRowView {
    var view = ScrollView();
    var content = CompositeView();
    view.hasBorder = false;
    view.hasVerticalScroller = false;
    view.canvas = content;
    view.fixedHeight = 130;
    content.layout = HLayout(nil);
    content.fixedHeight = 130;

    ^view;
  }

  makeParameterView {arg name, key, spec;
    var view = CompositeView();
    var label = StaticText();
    var valueLabel = StaticText();
    var knob = Knob();
    var currentPreset = this.parentSynthPage
    .parentSynthController
    .presetsManagerController
    .presetsManager
    .currentPreset;
    var selectedPreset = this.parentSynthPage
    .parentSynthController
    .presetsManagerController
    .presetsManager
    .selectedPreset;
    var previousColor;
    var dataKey = (this.key++'.'++key).asSymbol;
    var parentSynthController = this.parentSynthPage
    .parentSynthController;

    label.string = name?"";
    label.align = \center;
    valueLabel.align = \center;
    knob.fixedWidth = 40;
    knob.fixedHeight = 40;
    knob.mode = \vert;

    // apply spec and action to knob
    knob.action = {arg k;
      k.value!?{
        var newValue = spec.map(k.value);
        proxiesGroup.set(key, newValue);

        valueLabel.string = newValue.round(0.01).asString + (spec.units?"");

        // update preset
        currentPreset!?{
          currentPreset.setMapUIValueForKey(this.key, key, newValue);
        };
      };
    };

    knob.valueAction = selectedPreset!?{
      var value; 
      selectedPreset.getMapUIValues(this.key)!?{
        value = selectedPreset.getMapUIValues(this.key)[key];
      }??{
        value = spec.default;
      };
      spec.unmap(value);
    }??{
      var value = spec.default;
      spec.unmap(value);
    };

    // parameter bindings
    parentSynthController
    .data[dataKey]??{parentSynthController.data[dataKey] = ()};

    parentSynthController
    .data[dataKey][\updater] = {arg value;
      knob.valueAction = spec.unmap(value.value());
    };

    view.layout = VLayout(label, HLayout(knob), valueLabel);

    // midi learn related
    parentSynthController
    .prepareViewForMIDILearn(
      view, 
      dataKey
    );

    ^view;
  }
}
