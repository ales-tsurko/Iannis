(
// For development only
// ----------------------------------------------------------
Quarks.install("~/Desktop/Iannis".standardizePath);
Quarks.uninstall("Iannis");
// ----------------------------------------------------------
)

s.boot;

TempoClock.default.tempo = 107/60;

IannisProbabilisticSequencerView(\sequencer, \default, patternLength: 4).front;

Server.freeAll;
