+ IannisPresetsManagerController {

  didAddPreset {arg preset;
    this.presetsMenu.items = this.presetsMenu.items.add(preset.name);
  }

  didRemovePreset {arg preset, index;
  }

  didLoadPreset {arg preset, index;
    if (preset.isFactory) {
      this.updateButton.enabled = false;
      this.removeButton.enabled = false;
    } {
      this.updateButton.enabled = true;
      this.removeButton.enabled = true;
    };

    // update UI
    preset.values.keysValuesDo({arg key, value;
      this.parentController.parameterBinder[key].value(value);
    });
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
