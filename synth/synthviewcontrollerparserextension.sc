+ IannisSynthViewController {

  parse {
    metadata.keysDo({arg key;
      switch(key,
        \name, {this.parseName(metadata[\name])},
        \ui, {this.parseUI(metadata[\ui])},
        \presets, {this.parseFactoryPresets(metadata[\presets])}
      );
    });

    this.didFinishParsing();
  }

  parseName {arg name;
    this.synthName = name;
  }

  parseUI {arg uiObj;
    uiObj.keysDo({arg key;
      switch(key,
        \pages, {
          var numberOfPages = uiObj[\pages].size;
          uiObj[\pages].do({arg pageObj, index;
            this.parsePage(pageObj, numberOfPages, index);
          });
        }
      );
    });
  }

  parsePage {arg pageObj, numberOfPages, index;
    if (numberOfPages == 1 && pageObj[\name].isNil) {
      // hide tab bar if there is just one page 
      // and the former has no name
      this.addPage("tab");
      this.pagesView.tabsContainer.visible = false; 
    } {
      this.addPage(pageObj[\name]);
    };

    pageObj[\groups].do({arg groupObj;
      this.parseGroup(groupObj, index);
    });
  }

  parseGroup {arg groupObj, pageIndex;
    var newGroup;
    var groupName = groupObj[\name]?"";

    newGroup = IannisGroupView(groupName);
    newGroup.contentView.layout = VLayout();

    // hide header if name is not specified
    if (groupObj[\name].isNil) {
      newGroup.headerView.visible = false;
    } {
      newGroup.contentView.background = Color.gray(0.77);
    };

    // parse parameters
    groupObj[\parameters].do({arg parameter;
      if (parameter[\isRow].isNil) {
        var element = this.parseUIElement(parameter[\key], parameter[\name], parameter[\spec], parameter[\ui]);
        newGroup.contentView.layout.add(element)
      } {
        // if is row
        var align = parameter[\align];
        var parameters = parameter[\parameters];
        var row = this.parseRow(parameters, align);
        newGroup.contentView.layout.add(row);
      };
    });

    this.addGroupViewToPageAtIndex(newGroup, pageIndex);
  }

  parseRow {arg parametersArr, align;
    var view = CompositeView();
    view.layout = HLayout();

    parametersArr.do({arg obj;
      var element = this.parseUIElement(obj[\key], obj[\name], obj[\spec],obj[\ui]);
      view.layout.add(element);
    });

    ^this.parseAlignment(view, align);
  }

  parseUIElement {arg key, name, spec, uiObj;
    var view;

    switch(
      uiObj[\type],
      \recorder, {
        view = this.parseRecorderUI(key);
      },
      \popup, {
        view = this.parsePopupUI(key, name, uiObj);
      },
      \knob, {
        view = this.parseKnobUI(key, name, spec, uiObj);
      },
      \hslider, {
        view = this.parseSliderUI(key, name, spec, uiObj, \horizontal);
      },
      \vslider, {
        view = this.parseSliderUI(key, name, spec, uiObj, \vertical);
      },
      \env_adsr, {
        view = this.parseEnvADSRUI(key, name, uiObj);
      }
    );

    ^view;
  }

  parseRecorderUI {arg key;
    var newRecorder = IannisRecorderController("".resolveRelative);
    newRecorder.action = {arg recorder;
      node.set(key, recorder.value.bufnum);

      // update preset and synth values
      if (this.presetsManagerController.presetsManager.currentPreset.notNil) {
        this.presetsManagerController.presetsManager.currentPreset.values[key] = recorder.samplePath;
      };
    };

    // parameter bindings
    this.parameterBinder[key] = {arg value;
      var newValue = value.value();
      newRecorder.samplePath = newValue;
    };

    ^newRecorder;
  }

  parsePopupUI {arg key, name, uiObj;
    var view = CompositeView();
    var label = StaticText();
    var popup = PopUpMenu();
    var itemsAndValues = ();

    uiObj[\items].do({arg item;
      itemsAndValues[item[\name].asSymbol] = item[\value].value;
    });

    label.string = name;
    popup.fixedWidth = 150;
    popup.items = itemsAndValues.keys.asArray;
    popup.allowsReselection = true;

    popup.action = {arg p;
      var value = itemsAndValues.values[p.value];
      node.set(key, value);

      // update preset and synth values
      if (this.presetsManagerController.presetsManager.currentPreset.notNil) {
        this.presetsManagerController.presetsManager.currentPreset.values[key] = p.value;
      };
    };

    // parameter bindings
    this.parameterBinder[key] = {arg value;
      popup.valueAction = value.value();
    };

    view.layout = VLayout(label, popup);

    ^this.parseAlignment(view, uiObj[\align]);
  }

  parseKnobUI {arg key, name, spec, uiObj;
    var view = CompositeView();
    var label = StaticText();
    var valueLabel = StaticText();
    var knob = Knob();
    label.string = name;
    label.align = \center;
    valueLabel.align = \center;
    knob.fixedWidth = 40;
    knob.fixedHeight = 40;
    knob.mode = \vert;

    // apply spec and action to knob
    knob.action = {arg k;
      var newValue = spec.asSpec.map(k.value);
      node.set(key, newValue);
      valueLabel.string = newValue.round(0.01) + spec.asSpec.units;

      // update preset and synth values
      if (this.presetsManagerController.presetsManager.currentPreset.notNil) {
        this.presetsManagerController.presetsManager.currentPreset.values[key] = newValue;
      };
    };

    knob.valueAction = spec.asSpec.unmap(spec.asSpec.default);

    // parameter bindings
    this.parameterBinder[key] = {arg value;
      knob.valueAction = spec.asSpec.unmap(value.value());
    };

    view.layout = VLayout(label, HLayout(knob), valueLabel);

    ^this.parseAlignment(view, uiObj[\align]);
  }

  parseSliderUI {arg key, name, spec, uiObj, orientation;
    var view = CompositeView();
    var label = StaticText();
    var valueLabel = StaticText();
    var slider = Slider();
    label.string = name;

    // apply spec and action to knob
    slider.action = {arg sl;
      var newValue = spec.asSpec.map(sl.value);
      node.set(key, newValue);
      valueLabel.string = newValue.round(0.01) + spec.asSpec.units;

      // update preset and synth values
      if (this.presetsManagerController.presetsManager.currentPreset.notNil) {
        this.presetsManagerController.presetsManager.currentPreset.values[key] = newValue;
      };
    };

    slider.valueAction = spec.asSpec.unmap(spec.asSpec.default);

    // parameter bindings
    this.parameterBinder[key] = {arg value;
      slider.valueAction = spec.asSpec.unmap(value.value());
    };

    // layout
    if (orientation == \vertical) {
      slider.fixedWidth = 25;
      slider.fixedHeight = 160;

      label.align = \center;
      valueLabel.align = \center;
      view.layout = VLayout(label, HLayout(slider), valueLabel);
    } {
      slider.orientation = orientation;
      slider.fixedWidth = 160;
      slider.fixedHeight = 25;

      label.align = \right;
      valueLabel.align = \left;
      valueLabel.fixedWidth = 63;

      view.layout = HLayout(label, slider, valueLabel);
    }

    ^this.parseAlignment(view, uiObj[\align]);
  }

  parseEnvADSRUI {arg key, name, uiObj;
    var view = EnvelopeView();
    var curves = 0!3;
    var index = 0, previousY = 0;
    var nodeSpec = ControlSpec(0.001, 50, 2);
    view.fixedHeight = 200;
    view.fixedWidth = 580;
    view.setEnv(Env.adsr());
    view.curves = curves;
    view.setEditable(0, false);
    view.keepHorizontalOrder = true;
    view.gridOn_(true);
    view.grid = 0.1@0;
    view.gridColor = Color.gray(0.8);
    view.step = 0;

    view.action = {arg env;
      var newValues = env.value;
      var times = env.value[0].differentiate;
      var outputValue = [];

      outputValue = outputValue.add(nodeSpec.map(times[1])); // add attack
      outputValue = outputValue.add(nodeSpec.map(times[2])); // add decay
      outputValue = outputValue.add(newValues[1][2]); // add sustain
      outputValue = outputValue.add(nodeSpec.map(times[3])); // add release
      outputValue = outputValue.addAll(curves);

      // update node
      node.set(key, outputValue);

      // update UI
      env.setString(1, "attack = "++outputValue[0].round(0.001));
      env.setString(2, "decay = "++outputValue[1].round(0.001)++"; sustain = "++outputValue[2].round(0.001));
      env.setString(3, "release = "++outputValue[3].round(0.001));

      // disable y movements for attack and release
      newValues[1][1] = 1; // attack is always 1 on Y-axis
      newValues[1][newValues[1].size-1] = 0; // release is always 0 on Y-axis

      index = env.selection[0]?0;

      env.value = newValues;
      
      // update preset and synth values
      if (this.presetsManagerController.presetsManager.currentPreset.notNil) {
        this.presetsManagerController.presetsManager.currentPreset.values[key] = outputValue;
      }
    };

    view.valueAction = [[0,0.01,0.1,0.2],[0,1,0.5,0]];

    //
    // change curve with ctrl+drag
    //
    view.metaAction = {arg env;
      view.editable = false;
      index = env.selection[0];
    };

    view.mouseDownAction = {arg v, x, y;
      view.editable = true;
      previousY = y/v.bounds.height;
    };

    view.mouseMoveAction = {arg v, x, y;
      if ((view.editable.not) && (index > 0)) {
        var deltaY = previousY - (y/v.bounds.height);
        deltaY = deltaY * 15;

        if (index == 1) {
          // if the segment is attack - inverse
          deltaY = deltaY.neg;
        };

        curves[index-1] = curves[index-1] + deltaY;
        view.curves = curves;

        previousY = y/v.bounds.height;

        // update preset
      if (this.presetsManagerController.presetsManager.currentPreset.notNil) {
        curves.do({arg i, n;
          this.presetsManagerController.presetsManager.currentPreset.values[key][n+4] = i;
        });

        // update node
        node.set(key, this.presetsManagerController.presetsManager.currentPreset.values[key]);
      }
      }
    };

    // parameter bindings
    this.parameterBinder[key] = {arg value;
      var newValue = value.value();
      var x = [0];
      var y = [0,1,newValue[2]/*sustain*/,0];
      x = x.add(nodeSpec.asSpec.unmap(newValue[0])); // attack
      x = x.add(nodeSpec.asSpec.unmap(newValue[1])); // decay
      x = x.add(nodeSpec.asSpec.unmap(newValue[3])); // release
      x = x.integrate;

      curves = newValue[4..6];
      view.curves = curves;

      view.valueAction = [x,y];
    };

    // return
    ^this.parseAlignment(view, uiObj[\align]);
  }

  parseAlignment {arg view, align;
    var retLayout;
    var alignment = align?\center;

    switch(alignment,
      \left, {retLayout = HLayout(view, nil)},
      \center, {retLayout = HLayout(nil, view, nil)},
      \right, {retLayout = HLayout(nil, view)}
    );

    ^retLayout;
  }

  parseFactoryPresets {arg presetsObj;
    presetsObj.do({arg obj;
      var preset = IannisPreset(obj);
      this.presetsManagerController.presetsManager.addPreset(preset);
    });
  }
}
