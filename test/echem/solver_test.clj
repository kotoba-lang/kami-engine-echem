(ns echem.solver-test
  (:require [clojure.test :refer [deftest is testing]]
            [echem.solver :as fc]
            [cae.solver :as cae]))

(deftest realistic-polarization
  (testing "cell voltage and LHV efficiency land in the real PEM band"
    (let [r (fc/solve {})]
      (is (< 0.55 (:v-cell r) 0.85) (str "V_cell=" (:v-cell r)))
      (is (< 0.45 (:eff-LHV r) 0.68))
      (is (< 60 (:stack-kW r) 160)))))

(deftest higher-current-lower-voltage
  (testing "drawing more current density drops cell voltage (polarization)"
    (is (> (:v-cell (fc/solve {:i-density 0.6}))
           (:v-cell (fc/solve {:i-density 1.4}))))))

(deftest more-platinum-helps
  (testing "higher Pt loading lowers activation loss → higher voltage"
    (is (< (:v-cell (fc/solve {:pt-loading 0.1}))
           (:v-cell (fc/solve {:pt-loading 0.4}))))))

(deftest registered-on-contract
  (is (cae/registered? :rom-fc))
  (is (= :rom-fc (:solver (cae/solve {:solver {:kind :rom-fc}})))))

(deftest datafied
  (is (pos? (:datom-count (fc/run {:case/id "sedan/fc"})))))

(deftest effpct-datom-is-a-real-percent
  ;; effPct must be eff-LHV expressed as a 0-100 percent, not the fraction
  ;; scaled by 1000 -- a fuel cell can't be 520% efficient.
  (let [r (fc/run {:case/id "pct-check"})
        eff-pct (some (fn [[_ attr v]] (when (= attr :echem.FuelCellRun/effPct) v)) (:datoms r))]
    (is (= (Math/round (* 100.0 (:eff-LHV r))) eff-pct))
    (is (< 0 eff-pct 100) (str "effPct=" eff-pct " must be a sane percent, not eff*1000"))))
