IannisMixerTrack {
  var <node,
  <>name,
  <bus,
  <isSolo = false,
  <isMute = false,
  <instrumentsManager,
  <effectsManager,
  <gain = 0,
  <pan = 0;

  *new {arg name;
    ^super.new.init(name);
  }

  init {arg aName;
    name = aName?"New Track";
    bus = Bus.control(Server.default, 4);
    node = Synth(
      "by.alestsurko.iannis.track.controller",
      [\gain, 0, \pan, 0, \levelbus, bus.index]
    );

    instrumentsManager = IannisInstrumentsManager(this);
    effectsManager = IannisEffectsManager(this);

    effectsManager.group.moveBefore(this.node);
  }

  cleanUp {
    instrumentsManager.cleanUp();
    node.free();
  }

  gain_ {arg newValue;
    gain = newValue;

    if (isMute) {
      node.set(\gain, -inf);
    } {
      node.set(\gain, gain);
    };
  }

  pan_ {arg newValue;
    pan = newValue;
    node.set(\pan, pan);
  }

  isMute_ {arg newValue;
    var gainVal = this.gain;
    isMute = newValue;

    // trigger gain
    this.gain = gainVal;

    // stop synth
    // instrumentsManager.synthViewController!?{
      // instrumentsManager.synthViewController.node.run(isMute.not);
    // };

    // stop track controller
    // node.run(isMute.not);
  }

  isSolo_ {arg newValue;
    isSolo = newValue;
  }
}

// Instruments manager delegate
+ IannisMixerTrack {
  didSelectInstrument {arg instrumentDesc, synthViewController;
    this.effectsManager.group.moveAfter(synthViewController.node);
    node.moveAfter(this.effectsManager.group);
  }
}

// Effects manager delegate
+ IannisMixerTrack {
  didAddEffect {arg effect;
  }

  willRemoveEffectAtIndex {arg index, effectViewController;
  }

  didChangeEffectAtIndex {arg index, effectViewController;
  }

  didMoveEffectToIndex {arg fromIndex, toIndex;
  }
}
