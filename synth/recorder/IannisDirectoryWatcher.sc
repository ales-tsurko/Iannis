IannisDirectoryWatcher {
  var path, <>delegate,
  directorySnapshot,
  watcher;

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

  // startWatch {
    // watcher??{
      // watcher = SkipJack({
          // if (directorySnapshot != path.files.collect(_.fileName)) {
            // delegate.didChangeDirectoryContent();
            // directorySnapshot = path.files.collect(_.fileName);
          // };
        // }, 3);
    // };
// 
    // watcher.start();
  // }
// 
  // stopWatch {
    // watcher.stop();
  // }
  startWatch {
    if (watcher.isPlaying.not) {
      watcher = Routine({
        loop {
          if (directorySnapshot != path.files.collect(_.fileName)) {
            delegate.didChangeDirectoryContent();
            directorySnapshot = path.files.collect(_.fileName);
          };

          3.wait;
        };
      });
      AppClock.play(watcher);
    } {
      ("Already watching"+this.path).inform;
    }
  }

  stopWatch {
    watcher.stop();
  }
}
