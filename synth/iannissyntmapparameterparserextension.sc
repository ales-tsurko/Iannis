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

    case
    // the parameter has name
    {nameAndParamSplit.size == 2} {
      var name = nameAndParamSplit[0];
      var key = nameAndParamSplit[1].findRegexp("\\w+")[0][1].asSymbol;
      var spec = this.parseSpec(nameAndParamSplit[1]);
      view = this.makeParameterView(name, key, spec);
    }
    // the parameter has no name
    {nameAndParamSplit.size == 1} {
      var key = nameAndParamSplit[0].findRegexp("\\w+")[0][1].asSymbol;
      var spec = this.parseSpec(nameAndParamSplit[0]);
      view = this.makeParameterView(nil, key, spec);
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
    // content.background = Color.gray(0.77);

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

    label.string = name?"";
    label.align = \center;
    valueLabel.align = \center;
    knob.fixedWidth = 40;
    knob.fixedHeight = 40;
    knob.mode = \vert;

    // apply spec and action to knob
    knob.action = {arg k;
      var preset;
      var newValue = spec.map(k.value);
      nodeProxy.set(key, newValue);

      valueLabel.string = newValue.round(0.01).asString + (spec.units?"");

      // update preset
      currentPreset!?{
        currentPreset.setMapUIValueForKey(this.key, key, newValue);
      };
    };

    knob.valueAction = selectedPreset!?{
      var value = selectedPreset.getMapUIValues(this.key)[key];
      spec.unmap(value);
    }??{
      var value = spec.default;
      spec.unmap(value);
    };

    // parameter bindings
    this.parameterBinder[key] = {arg value;
      knob.valueAction = spec.unmap(value.value());
    };

    view.layout = VLayout(label, HLayout(knob), valueLabel);

    ^view;
  }
}
