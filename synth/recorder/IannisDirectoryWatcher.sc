IannisDirectoryWatcher {
  var path, <>delegate,
  directorySnapshot,
  watcherRout;

  *new {arg path, delegate;
    ^super.new.init(path, delegate);
  }

  init {arg aPath, aDelegate;
    delegate = aDelegate;
    path = PathName(aPath);
  }

  path_ {arg newPath;
    path = PathName(newPath);
  }

  path {
    ^path.fullPath;
  }

  startWatch {
    if (watcherRout.isPlaying.not) {
      watcherRout = Routine({
        loop {
          if (directorySnapshot != path.files.collect(_.fileName)) {
            delegate.didChangeDirectoryContent();
            directorySnapshot = path.files.collect(_.fileName);
          };

          3.wait;
        };
      });
      AppClock.play(watcherRout);
    } {
      ("Already watching"+this.path).inform;
    }
  }

  stopWatch {
    watcherRout.stop();
  }

}
