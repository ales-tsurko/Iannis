IannisNodeGroup : Group {
  var <getState, 
  // used as a container for the voices
  // instantiated by the midi client
  <>midiVoices;

  *new {arg target, addAction = 'addToHead';
    ^super.new(target, addAction).init();
  }

  init {
    midiVoices = nil!127;
    getState = ();
  }

  set {arg ...args;
    super.set(*args);

    args.do({arg item, n;
      if (n.even) {
        getState[item] = args[n+1]
      }
    });
  }

  releaseMIDIVoices {
    this.midiVoices.do(_.release);
  }
}
