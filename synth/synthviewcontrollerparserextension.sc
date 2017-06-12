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
      var view = this.parseGroupParameter(parameter);
      newGroup.contentView.layout.add(view);
    });

    this.addGroupViewToPageAtIndex(newGroup, pageIndex);
  }

  parseGroupParameter {arg parameter;
    var view;

    if ((parameter[\isRow].isNil) && (parameter[\tabs].isNil)) {
      view = this.parseUIElement(parameter[\key], parameter[\name], parameter[\spec], parameter[\ui]);
    };

    // if is row
    parameter[\isRow]!?{
      var align = parameter[\align];
      var parameters = parameter[\parameters];
      view = this.parseRow(parameters, align);
    };

    // if is tabbed element
    parameter[\tabs]!?{
      view = this.parseTabbedElement(parameter[\tabs]);
    };

    ^view;
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

  parseTabbedElement {arg tabs;
    var view;
    tabs.do({arg tab;
      var name = tab[\name];
      var container = CompositeView();
      container.layout = VLayout();

      view!?{
        view.addPage(name, container);
      }??{
        view = IannisTabbedUIElement(name, container);
      };

      tab[\parameters].do({arg parameter;
        var element = this.parseGroupParameter(parameter);
        container.layout.add(element);
      });
    });

    ^view;
  }

  parseUIElement {arg key, name, spec, uiObj;
    var view;

    this.data[key] = ();
    this.data[key][\spec] = spec?ControlSpec();

    switch(
      uiObj[\type],
      \recorder, {
        view = this.parseRecorderUI(key);
      },
      \button, {
        view = this.parseButtonUI(key, uiObj);
      },
      \check, {
        view = this.parseCheckUI(key, name, uiObj);
      },
      \number, {
        view = this.parseNumberUI(key, name, spec, uiObj);
      },
      \hrslider, {
        view = this.parseRangeSliderUI(key, name, spec, uiObj, \horizontal);
      },
      \vrslider, {
        view = this.parseRangeSliderUI(key, name, spec, uiObj, \vertical);
      },
      \xy, {
        view = this.parseXYUI(key, name, spec, uiObj);
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
      },
      \custom, {
        view = this.parseCustomUIElement(key, name, uiObj);
      }
    );

    ^view;
  }

  parseRecorderUI {arg key;
    var newRecorder = IannisRecorderController("".resolveRelative);
    newRecorder.action = {arg recorder;
      this.updateNodeValue(key, recorder.value.bufnum);

      // update preset and synth values
      if (this.presetsManagerController.presetsManager.currentPreset.notNil) {
        this.presetsManagerController.presetsManager.currentPreset.values[key] = recorder.samplePath;
      };
    };

    // parameter bindings
    this.data[key][\updater] = {arg value;
      var newValue = value.value();
      newRecorder.samplePath = newValue;
    };

    this.data[key][\view] = newRecorder;

    ^newRecorder;
  }

  parseButtonUI {arg key, uiObj;
    var view = CompositeView();
    var button = Button();
    var states = [];

    uiObj[\states].do({arg item;
      states = states.add([item[\name]]);
    });

    button.fixedWidth = 150;
    button.states = states;

    button.action = {arg but;
      var value = uiObj[\states][but.value][\value].value();
      this.updateNodeValue(key, value);

      // update preset
      this.presetsManagerController.presetsManager.currentPreset!?{
        this.presetsManagerController.presetsManager.currentPreset.values[key] = but.value;
      }
    };

    // parameter bindings
    this.data[key][\updater] = {arg value;
      button.valueAction = value.value();
    };

    // layout
    view.layout = VLayout(button);

    this.data[key][\view] = view;
    ^this.parseAlignment(view, uiObj[\align]);
  }

  parseCheckUI {arg key, name, uiObj;
    var view = CompositeView();
    var check = CheckBox();
    check.string = name;

    check.action = {arg ch;
      var value = ch.value.asInteger;

      this.updateNodeValue(key, value);

      // update preset
      this.presetsManagerController.presetsManager.currentPreset!?{
        this.presetsManagerController.presetsManager.currentPreset.values[key] = ch.value;
      }
    };

    // parameter bindings
    this.data[key][\updater] = {arg value;
      check.valueAction = value.value();
    };

    // layout
    view.layout = VLayout(check);

    this.data[key][\view] = view;
    ^this.parseAlignment(view, uiObj[\align]);
  }

  parseNumberUI {arg key, name, spec, uiObj;
    var view = CompositeView();
    var number = NumberBox();
    var label = StaticText();
    label.string = name + "("++spec.asSpec.units++")";

    number.fixedWidth = 65;
    number.clipLo = spec.clipLo;
    number.clipHi = spec.clipHi;

    if (spec.asSpec.step == 0) {
      number.step = 0.01;
      number.scroll_step = 0.01;
      number.maxDecimals = 4;
    } {
      if (spec.asSpec.step < 1) {
        number.maxDecimals = 4;
      } {
        number.decimals = 0;
      };

      number.step = spec.asSpec.step;
      number.scroll_step = spec.asSpec.step;
    };

    number.action = {arg nb;
      var value = nb.value;

      this.updateNodeValue(key, value);

      // update preset
      this.presetsManagerController.presetsManager.currentPreset!?{
        this.presetsManagerController.presetsManager.currentPreset.values[key] = nb.value;
      };
    };

    // parameter bindings
    this.data[key][\updater] = {arg value;
      number.valueAction = value.value();
    };

    // layout
    view.layout = VLayout(label, number);

    this.data[key][\view] = view;
    ^this.parseAlignment(view, uiObj[\align]);
  }

  parseRangeSliderUI {arg key, name, spec, uiObj, orientation;
    var view = CompositeView();
    var slider = RangeSlider();
    var label = StaticText();
    var valueLabel = StaticText();
    label.string = name + "("++spec.asSpec.units++")";

    slider.action = {arg sl;
      var value = 0!2;

      value[0] = spec.asSpec.map(sl.lo);
      value[1] = spec.asSpec.map(sl.hi);

      valueLabel.string = value[0].round(0.01).asString ++ ";" + value[1].round(0.01).asString;

      this.updateNodeValue(key, value);

      // update preset
      this.presetsManagerController.presetsManager.currentPreset!?{
        this.presetsManagerController.presetsManager.currentPreset.values[key] = value;
      };
    };

    // parameter bindings
    this.data[key][\updater] = {arg value;
      slider.lo = spec.asSpec.unmap(value.value[0]);
      slider.hi = spec.asSpec.unmap(value.value[1]);

      // don't know why, but it's working only when calling twice
      // in other case the first value that was set, becomes a clipped
      // version of the same parameter of the previous preset... looks
      // like a bug in the RangeSlider. Also it's maybe a cause of calling
      // tasks synchronously. Anyway, here it's working and it's call 
      // action only once, so it's a fine solution.
      slider.setSpanActive(
        spec.asSpec.unmap(value.value[0]),
        spec.asSpec.unmap(value.value[1])
      );
    };

    // layout
    if (orientation == \vertical) {
      slider.fixedWidth = 25;
      slider.fixedHeight = 160;

      label.align = \center;
      valueLabel.align = \center;
    } {
      slider.orientation = orientation;
      slider.fixedWidth = 160;
      slider.fixedHeight = 25;

      label.align = \left;
      valueLabel.align = \left;
    };

    view.layout = VLayout(label, HLayout(slider), valueLabel);

    this.data[key][\view] = view;
    ^this.parseAlignment(view, uiObj[\align]);
  }

  parseXYUI {arg key, name, spec, uiObj;
    var view = CompositeView();
    var xy = Slider2D();
    var label = StaticText();
    var valueLabel = StaticText();
    label.string = name;

    xy.fixedHeight = 160;

    xy.action = {arg v;
      var value = 0!2;

      value[0] = spec[0].asSpec.map(v.x);
      value[1] = spec[1].asSpec.map(v.y);

      valueLabel.string = value[0].round(0.01).asString + spec[0].asSpec.units ++ ";" + value[1].round(0.01).asString + spec[1].asSpec.units;

      this.updateNodeValue(key, value);

      // update preset
      this.presetsManagerController.presetsManager.currentPreset!?{
        this.presetsManagerController.presetsManager.currentPreset.values[key] = [v.x, v.y];
      };
    };

    this.data[key][\updater] = {arg value;
      xy.setXYActive(value.value[0], value.value[1]);
    };

    // layout
    view.layout = VLayout(label, xy, valueLabel);

    this.data[key][\view] = view;
    ^this.parseAlignment(view, uiObj[\align]);
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

      this.updateNodeValue(key, value);

      // update preset and synth values
      if (this.presetsManagerController.presetsManager.currentPreset.notNil) {
        this.presetsManagerController.presetsManager.currentPreset.values[key] = p.value;
      };
    };

    // parameter bindings
    this.data[key][\updater] = {arg value;
      popup.valueAction = value.value();
    };

    view.layout = VLayout(label, popup);

    this.data[key][\view] = view;
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
      valueLabel.string = newValue.round(0.01) + spec.asSpec.units;
      this.updateNodeValue(key, newValue);

      // update preset
      if (this.presetsManagerController.presetsManager.currentPreset.notNil) {
        this.presetsManagerController.presetsManager.currentPreset.values[key] = newValue;
      };
    };

    knob.valueAction = spec.asSpec.unmap(spec.asSpec.default);

    // parameter bindings
    this.data[key][\updater] = {arg value;
      knob.valueAction = spec.asSpec.unmap(value.value());
    };

    view.layout = VLayout(label, HLayout(knob), valueLabel);

    this.data[key][\view] = view;
    ^this.parseAlignment(view, uiObj[\align]);
  }

  parseCustomUIElement {arg key, name, uiObj;
    var view = uiObj[\init].value(key, name, uiObj);
    view.action = {arg view;
      var presetValue;

      this.mapView.parametersMaps[key]!?{
        if (this.mapView.parametersMaps[key].isOn) {
          var aKey = \selfvalue;
          var aNode = this.mapView.parametersMaps[key].proxiesGroup;
          presetValue = uiObj[\action].value(view, aKey, aNode, uiObj);
        } {
          presetValue = uiObj[\action].value(view, key, node, uiObj);
        }
      }??{
          presetValue = uiObj[\action].value(view, key, node, uiObj);
      };

      // update preset
      if (this.presetsManagerController.presetsManager.currentPreset.notNil) {
        this.presetsManagerController.presetsManager.currentPreset.values[key] = presetValue;
      };
    };

    this.data[key][\updater] = {arg value;
      uiObj[\binder].value(view, value, uiObj);
    };

    this.data[key][\view] = view;
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
      valueLabel.string = newValue.round(0.01) + spec.asSpec.units;
      
      this.updateNodeValue(key, newValue);

      // update preset and synth values
      if (this.presetsManagerController.presetsManager.currentPreset.notNil) {
        this.presetsManagerController.presetsManager.currentPreset.values[key] = newValue;
      };
    };

    slider.valueAction = spec.asSpec.unmap(spec.asSpec.default);

    // parameter bindings
    this.data[key][\updater] = {arg value;
      slider.valueAction = spec.asSpec.unmap(value.value());
    };

    // layout
    if (orientation == \vertical) {
      slider.fixedWidth = 25;
      slider.fixedHeight = 160;

      label.align = \center;
      valueLabel.align = \center;
    } {
      slider.orientation = orientation;
      slider.fixedWidth = 160;
      slider.fixedHeight = 25;

      label.align = \left;
      valueLabel.align = \left;
    };

    view.layout = VLayout(label, HLayout(slider), valueLabel);

    this.data[key][\view] = view;
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
      this.updateNodeValue(key, outputValue);

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
        this.updateNodeValue(key, this.presetsManagerController.presetsManager.currentPreset.values[key]);
      }
      }
    };

    // parameter bindings
    this.data[key][\updater] = {arg value;
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

    this.data[key][\view] = view;
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

  updateNodeValue {arg key, value;
      // unfortunatelly, it's impossible to write just
      // if (object.notNil && object.isOn) {  }
      // so:
      this.mapView.parametersMaps[key]!?{
        if (this.mapView.parametersMaps[key].isOn) {
          this.mapView.parametersMaps[key].proxiesGroup.set(\selfvalue, value);
        } {
          node.set(key, value);
        }
      }??{
        node.set(key, value);
      }
    }
}
