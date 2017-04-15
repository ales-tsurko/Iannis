IannisSynthMetadataParser {
  var <synthName, <metadata, view;

  *new {arg synthName;
    ^super.new.init(synthName);
  }

  init {arg nameOfSynth;
    synthName = nameOfSynth;

    metadata = SynthDescLib.getLib('iannis_synth').at(synthName).metadata;

    view = IannisSynthViewController();
  }

  parse {
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
          uiObj[\pages].do({arg pageObj, index;
            this.parsePage(pageObj);
          });
        }
      );
    });
  }

  parsePage {arg pageObj, index;
    if (pageObj[\name].notNil) {
      view.addPage(pageObj[\name]);
    };

    pageObj[\groups].do({arg groupObj;
      this.parseGroup(groupObj, view.pages[index]);
    });
  }

  parseGroup {arg groupObj, view;
  }

  parsePresets {arg presetsObj;
  }

}
