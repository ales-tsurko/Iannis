IannisPresetsManager {
  var <presets, <currentPreset, delegate, <userPresetsPath;

  *new {arg delegate, userPresetsPath;
    ^super.new.init(delegate, userPresetsPath);
  }

  init {arg aDelegate, presetsPath;
    delegate = aDelegate;
    userPresetsPath = presetsPath;
  }

  addPreset {arg preset;
    presets = presets.add(preset);
    delegate.didAddPreset(preset);
  }

  removePreset {arg index;
    if (this.presets[index].isFactory.not) {
      delegate.didRemovePreset(presets.removeAt(index), index);
    } {
      "Can't remove a factory preset".inform;
    };
  }

  loadPreset {arg index;
    currentPreset = this.presets[index];

    delegate.didLoadPreset(this.currentPreset, index);
  }

  updatePresetAtIndexWithPreset {arg index, preset;
    if (this.presets[index].isFactory.not) {
      presets[index] = preset;

      delegate.didUpdatePresetAtIndexWithPreset(index, preset);

      this.writeUserPresetsToDisk(this.userPresetsPath);
    } {
      "Can't update a factory preset".inform;
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

  writeUserPresetsToDisk {
    try {
      this.userData.writeBinaryArchive(this.userPresetsPath);

      delegate.didWriteUserPresetsToDisk();
    } {arg err;
      ("Data didn't written:" + err.errorString).error;
    }
  }

  loadUserPresetsFromDisk {
    try {
      var data = this.readBinaryArchive(this.userPresetsPath);
      data.do({
        var preset = IannisPreset(data);
        presets = presets.add(preset);
      });

      delegate.didLoadUserPresetsFromDisk();
    } {arg err;
      ("Data not loaded:" + err.errorString).error;
    }
  }
}
