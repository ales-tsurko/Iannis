IannisNodeGroup : Group {
  var <getState;

  *new {arg target, addAction = 'addToHead';
    ^super.new(target, addAction).init();
  }

  init {
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
}
