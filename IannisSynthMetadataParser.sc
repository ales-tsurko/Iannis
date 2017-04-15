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
    newGroup.contentView.fixedHeight = 200;
    };

    // parse parameters
    groupObj[\parameters].do({arg parameter;
      this.parseGroupParameter(parameter, newGroup.contentView);
    });

    view.addGroupViewToPageAtIndex(newGroup, pageIndex);
  }

  parseGroupParameter {arg parameterObj, groupView; 
    if (parameterObj[\isRow].isNil) {
      switch(
        parameterObj[\ui][\type],
        \recorder, {groupView.layout.add(this.parseRecorderUI())}
      );
    } {
      // parse row
    };
  }

  parseRecorderUI {
    var newRecorder = IannisRecorderController("~/Desktop".standardizePath);
    // newRecorder.action = {arg recorder;
      // view.node.set(key, recorder.bufnum);
    // };
  ^newRecorder;
  }

  parsePresets {arg presetsObj;
    postln("parse presets:"+presetsObj);
  }

}
