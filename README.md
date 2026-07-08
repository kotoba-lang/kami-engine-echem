# kami-engine-echem

[![CI](https://github.com/kotoba-lang/kami-engine-echem/actions/workflows/ci.yml/badge.svg)](https://github.com/kotoba-lang/kami-engine-echem/actions/workflows/ci.yml)

Reduced-order PEM fuel-cell solver (`:rom-fc`) — polarization curve → cell voltage, LHV efficiency, stack power → real H2 consumption. The right 'combustion' analog for an FCEV is electrochemistry. A kami-echem CFD registers `:pemfc-cfd`.

Part of the clean-sheet vehicle-design / CAE stack (purpose-split shared libs).
Zero-dep portable `.cljc`. Run `clojure -M:test`.
