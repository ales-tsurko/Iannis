IannisMixerTrack {
  var <node,
  <>name,
  isSolo = false,
  <isMute = false,
  <instrumentsManager,
  effectsManager,
  <gain = 0,
  <pan = 0;

  *new {arg name;
    ^super.new.init(name);
  }

  init {arg aName;
    name = aName?"New Track";
    node = Synth(
      "by.alestsurko.iannis.track.controller",
      [\gain, 0, \pan, 0]
    );

    instrumentsManager = IannisInstrumentsManager(this);
  }

  cleanUp {
    instrumentsManager.cleanUp();
    node.free();
  }

  gain_ {arg newValue;
    gain = newValue;
    node.set(\gain, gain);
  }

  pan_ {arg newValue;
    pan = newValue;
    node.set(\pan, pan);
  }

  isMute_ {arg newValue;
    isMute = newValue;

    // stop synth
    instrumentsManager.synthViewController!?{
      instrumentsManager.synthViewController.node.run(isMute.not);
    };

    // stop track controller
    node.run(isMute.not);
  }

  isSolo_ {arg newValue;
    isSolo = newValue;
  }
}

// Instruments manager delegate
+ IannisMixerTrack {
  didSelectInstrument {arg instrumentDesc, synthViewController;
    node.moveAfter(synthViewController.node);
  }
}

// Effects manager delegate
+ IannisMixerTrack {
  didAddEffect {arg effect;
  }
}
