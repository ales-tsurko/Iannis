IannisNodeGroup : Group {
  var <getState, 
  // used as a container for the voices
  // instantiated by the midi client
  <>voices;

  set {arg ...args;
    super.set(*args);

    getState??{getState = ()};

    args.do({arg item, n;
      if (n.even) {
        getState[item] = args[n+1]
      }
    });
  }

  releaseVoices {
    this.voices.do(_.release);
  }
}
