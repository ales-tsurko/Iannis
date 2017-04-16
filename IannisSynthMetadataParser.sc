IannisSynthMetadataParser {
  var <synthName, <metadata, view, node;

  *new {arg metadata, node;
    ^super.new.init(metadata, node);
  }

  init {arg inMetadata, aNode;
    metadata = inMetadata;
    node = aNode;
  }

  parse {
    // init view
    view = IannisSynthViewController();

    metadata.keysDo({arg key;
      switch(key,
        \name, {this.parseName(metadata[\name])},
        \ui, {this.parseUI(metadata[\ui])},
        \factory_presets, {this.parsePresets(metadata[\factory_presets])}
      );
    });

    ^view;
  }

  parseName {arg name;
    view.synthName = name;
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
      view.addPage("tab");
      view.pagesView.tabsContainer.visible = false; 
    } {
      view.addPage(pageObj[\name]);
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

    view.addGroupViewToPageAtIndex(newGroup, pageIndex);
  }

  parseRow {arg parametersArr, align;
    var returnLayout;
    var view = CompositeView();
    view.layout = HLayout();

    parametersArr.do({arg obj;
      var element = this.parseUIElement(obj[\key], obj[\name], obj[\spec],obj[\ui]);
      view.layout.add(element);
    });

    switch(align,
      \left, {returnLayout = HLayout(nil, view)},
      \center, {returnLayout = HLayout(nil, view, nil)},
      \right, {returnLayout = HLayout(view, nil)}
    );
    ^returnLayout;
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
      }
    );

    ^view;
  }


  parseRecorderUI {arg key;
    var newRecorder = IannisRecorderController("~/Desktop".standardizePath);
    newRecorder.action = {arg recorder;
      node.set(key, recorder.value);
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

    popup.action = {arg p;
      var value = itemsAndValues.values[p.value];
      node.set(key, value);
    };

    view.layout = VLayout(label, popup);

    ^HLayout(view, nil);
  }

  parsePresets {arg presetsObj;
    postln("parse presets:"+presetsObj);
  }

  parseKnobUI {arg key, name, spec, uiObj;
    var view = CompositeView();
    var label = StaticText();
    var valueLabel = StaticText();
    var knob = Knob();
    // view.fixedWidth = 90;
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
    };

    knob.valueAction = spec.asSpec.unmap(spec.asSpec.default);

    view.layout = VLayout(label, HLayout(knob), valueLabel);

    ^view;
  }

}
