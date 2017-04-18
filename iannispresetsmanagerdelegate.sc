+ IannisPresetsManagerController {

  didAddPreset {arg preset;
    this.presetsMenu.items = this.presetsMenu.items.add(preset.name);
  }

  didRemovePreset {arg preset, index;
  }

  didLoadPreset {arg preset, index;
  }

  didWriteUserPresetsToDisk {
  }

  didLoadUserPresetsFromDisk {
  }

  didUpdatePresetAtIndexWithPreset {arg index, preset;
  }
}
