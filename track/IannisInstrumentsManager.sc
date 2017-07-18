IannisInstrumentsManager {
  classvar <availableInstrumentsDescs;
  var delegate,
  <currentInstrumentDesc,
  <synthViewController;

  *new {arg delegate;
    ^super.new.init(delegate);
  }

  init {arg aDelegate;
    delegate = aDelegate;
    IannisInstrumentsManager.availableInstrumentsDescs??{
      IannisInstrumentsManager.fetchAvailableInstruments();
    }
  }

  cleanUp {
    synthViewController.cleanUp();
  }

  *fetchAvailableInstruments {
    var synthDescs = SynthDescLib
    .getLib(\iannis_synth)
    .synthDescs
    .select({arg desc;
      desc.metadata[\type] == \synth;
    });

    availableInstrumentsDescs??{availableInstrumentsDescs = []};
    availableInstrumentsDescs = synthDescs;
  }

  *availableInstrumentsNames {
    var names = [];
    IannisInstrumentsManager.availableInstrumentsDescs??{
      IannisInstrumentsManager.fetchAvailableInstruments();
    };

    IannisInstrumentsManager.availableInstrumentsDescs.do({arg desc;
      names = names.add(desc.metadata[\name]);
    });

    ^names;
  }

  selectInstrument {arg instrumentName;
    currentInstrumentDesc!?{
      synthViewController.close();
    };

    currentInstrumentDesc = IannisInstrumentsManager
    .availableInstrumentsDescs[instrumentName];

    currentInstrumentDesc!?{
      synthViewController = IannisSynthViewController(
        currentInstrumentDesc.name
      );
    };

    delegate.didSelectInstrument(currentInstrumentDesc, synthViewController);
  }
}