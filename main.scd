(
// For development only
// ----------------------------------------------------------
LanguageConfig.removeIncludePath(Document.current.dir);
LanguageConfig.addIncludePath(Document.current.dir);
LanguageConfig.store;
thisProcess.recompile;
// ----------------------------------------------------------
)

TempoClock.default.tempo = 107/60;

IannisProbabilisticSequencerView(\sequencer, \default, patternLength: 4).front;

Server.freeAll;

