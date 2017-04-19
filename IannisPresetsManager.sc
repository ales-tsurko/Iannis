IannisPresetsManager {
  var <presets, <currentPreset, <selectedPreset,
  delegate, <userPresetsPath;

  *new {arg delegate;
    ^super.new.init(delegate);
  }

  init {arg aDelegate;
    delegate = aDelegate;
  }

  addPreset {arg preset;
    presets = presets.add(preset);
    delegate.didAddPreset(preset);
  }

  removePreset {arg index;
    if (this.presets[index].isFactory.not) {
      delegate.didRemovePreset(presets.removeAt(index), index);
    } {
      "Can't remove a factory preset".error;
    };
  }

  loadPreset {arg index;
    currentPreset = this.presets[index].deepCopy;
    selectedPreset = this.presets[index];

    delegate.didLoadPreset(this.selectedPreset, index);
  }

  updatePresetAtIndexWithPreset {arg index, preset;
    if (this.presets[index].isFactory.not) {
      presets[index] = preset;

      delegate.didUpdatePresetAtIndexWithPreset(index, preset);

      // as far as we know, that the preset is a user one,
      // the presets file already written, so we can
      // use this.userPresetsPath and write the file to the disk
      this.writeUserPresetsToDisk(this.userPresetsPath);
    } {
      "Can't update a factory preset".error;
    };
  }

  updateSelectedPresetWithCurrentData {
    if (this.selectedPreset.isFactory.not) {
      this.selectedPreset.values = this.currentPreset.values;
      this.writeUserPresetsToDisk(this.userPresetsPath);

      delegate.didUpdateSelectedPresetWithCurrentData();
    } {
      "Can't update a factory preset".error;
    };
  }

  getUserPresets {
    ^this.presets.select(_.isFactory.not);
  }

  userData {
    // get data for user presets only
    ^this.getUserPresets.collect(_.data);
  }

  getPresetsNames {
    ^this.presets.collect(_.name);
  }

  data {
   ^this.presets.collect(_.data); 
  }

  writeUserPresetsToDisk {arg path;
    try {
      this.userData.writeBinaryArchive(path);

      userPresetsPath = path;

      delegate.didWriteUserPresetsToDisk();
    } {arg err;
      ("Data didn't written:\n" + err.errorString).error;
    }
  }

  loadUserPresetsFromDisk {arg path;
    try {
      var data = Object.readBinaryArchive(path);

      data.do({arg item;
        var preset = IannisPreset(item);
        presets = presets.add(preset);
      });

      userPresetsPath = path;

      delegate.didLoadUserPresetsFromDisk();
    } {arg err;
      ("Data not loaded:\n" + err.errorString).error;
    }
  }
}
