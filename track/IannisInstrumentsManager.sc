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
    availableInstrumentsDescs = synthDescs.asArray;
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

  selectInstrument {arg index;
      if(index > -1) {
          this.selectInstrumentAtIndex(index);
      } {
          this.selectLiveCodeInstrument();

      };

    delegate.didSelectInstrument(currentInstrumentDesc, synthViewController);
  }

  selectInstrumentAtIndex {arg index;
      currentInstrumentDesc!?{
          synthViewController.close();
      };

      currentInstrumentDesc = IannisInstrumentsManager
      .availableInstrumentsDescs[index];

      currentInstrumentDesc!?{
          synthViewController = IannisSynthViewController(
              currentInstrumentDesc.name,
              delegate.innerBus
          );
      };
  }

  selectLiveCodeInstrument {
      currentInstrumentDesc!?{
          synthViewController.close();
      };

      currentInstrumentDesc = ();

      currentInstrumentDesc!?{
          synthViewController = IannisSynthViewController(
              nil,
              delegate.innerBus
          );
      };
  }
}
