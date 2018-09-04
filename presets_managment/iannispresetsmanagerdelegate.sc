+ IannisPresetsManagerController {

  didAddPreset {arg preset;
    this.presetsMenu.items = this.presetsMenu.items.add(preset.name);
  }

  didRemovePreset {arg preset, index;
    var presetsPath = this.presetsManager.userPresetsPath;
    var items = this.presetsMenu.items;
    var nextIndex = this.presetsMenu.value;
    // update presets menu
    items.removeAt(index);
    this.presetsMenu.items = items;
    // select the next preset
    this.presetsMenu.valueAction = nextIndex.clip(0, items.size-1);
    // update the presets file
    this.presetsManager.writeUserPresetsToDisk(presetsPath);
  }

  didLoadPreset {arg preset, index;
    if (preset.isFactory) {
      this.updateButton.enabled = false;
      this.removeButton.enabled = false;
    } {
      this.updateButton.enabled = true;
      this.removeButton.enabled = true;
    };

    // update main view
    preset.values.keysValuesDo({arg key, value;
      var data = this.parentController.data[key];
      data!?{
        data[\updater].value(value);
      };
    });

    // update map view
    this.parentController.mapView!?{
        this.parentController.mapView.onLoadPreset(preset);
    };

    this.parentController.liveCodeEditor!?{
        this.parentController.liveCodeEditor.onLoadPreset(preset);
    };

    // update midi view
    this.parentController.midiView.onLoadPreset(preset);
  }

  didWriteUserPresetsToDisk {
  }

  didLoadUserPresetsFromDisk {
  }

  didUpdatePresetAtIndexWithPreset {arg index, preset;
  }

  didUpdateSelectedPresetWithCurrentData {
  }
}
