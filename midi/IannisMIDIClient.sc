IannisMIDIClient : MIDIClient {
  classvar onUpdateMIDISources,
  sourcesWatcher, midiSourcesListSnaphot;

  *init {arg inports, outports, verbose = true;
    MIDIClient.init(inports, outports, verbose);

    midiSourcesListSnaphot = IannisMIDIClient.sources;
    
    onUpdateMIDISources.value();

    // sources watcher
    sourcesWatcher = Routine({
      loop {
        IannisMIDIClient.list();

        if (IannisMIDIClient.sources.size > 0) {
          if (midiSourcesListSnaphot.last.uid != IannisMIDIClient.sources.last.uid || midiSourcesListSnaphot.size != IannisMIDIClient.sources.size) {
            midiSourcesListSnaphot = IannisMIDIClient.sources();
            onUpdateMIDISources.value();
          };
        };

        2.wait;
      }
    });

    SystemClock.play(sourcesWatcher);
  }

  *disposeClint {
    MIDIClient.disposeClint();
    sourcesWatcher.stop();
  }

  *restart {
    MIDIClient.restart();
    sourcesWatcher.stop();
  }

  *addOnUpdateSourcesAction {arg func;
    onUpdateMIDISources = onUpdateMIDISources.addFunc(func);
  }

  *removeOnUpdateSourcesAction {arg func;
    onUpdateMIDISources = onUpdateMIDISources.removeFunc(func);
  }
}
