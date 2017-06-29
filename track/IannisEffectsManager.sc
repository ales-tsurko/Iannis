IannisEffectsManager {
  classvar availableEffects;
  var delegate,
  effectsViewControllers,
  group;

  *new {arg delegate;
    ^super.new.init(delegate);
  }

  init {arg aDelegate;
    delegate = aDelegate;
    group = Group();

    IannisEffectsManager.availableEffects??{
      IannisEffectsManager.fetchAvailableEffects();
    };

    effectsViewControllers = [];
  }

  cleanUp {
    effectsViewControllers.do({arg viewController;
      viewController.cleanUp();
    });

    group.free();
  }

  *fetchAvailableInstruments {
    var synthDescs = SynthDescLib
    .getLib(\iannis_synth)
    .synthDescs
    .select({arg desc;
      desc.metadata[\type] == \effect;
    });

    availableEffects??{availableEffects = []};
    availableEffects = synthDescs;
  }

  addEffect {arg effectName;

    delegate.didAddEffect();
  }
}
