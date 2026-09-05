(ns echem.run-at-power-test
  (:require [clojure.test :refer [deftest is testing]]
            [echem.solver :as fc]
            [cae.solver :as cae]))

(deftest run-at-power-logs-the-solved-operating-point
  (testing "the datafied run carries the power-targeted point, not the fixed-default one"
    (let [target 80.0
          r      (fc/run-at-power {:case/id "veh-sedan-fcev/fc"} target)
          op     (fc/solve-at-power {} target)]
      (is (:feasible r))
      ;; the logged stack power is the demanded power, hit by the inversion
      (is (= (:stack-kW op) (:stack-kW r)))
      (is (= (:i-density op) (:i-density r)))
      (is (= target (:p-stack-kW-target r)))
      ;; and it differs from the fixed-default operating point run logs
      (let [d (fc/run {:case/id "fixed-default"})]
        (is (not= (:i-density d) (:i-density r))
            "80 kW demand does not sit at the default 1.2 A/cm²")))))

(deftest run-at-power-datoms-are-consistent
  (testing "logged datoms agree with the returned operating point"
    (let [r   (fc/run-at-power {:case/id "pct-at-power"} 90.0)
          dat (into {} (map (fn [[_e a v]] [a v])) (:datoms r))]
      (is (pos? (:datom-count r)))
      (is (= (Math/round (* 1000.0 (:v-cell r))) (:echem.FuelCellRun/vCellMv dat)))
      (is (= (Math/round (* 100.0 (:eff-LHV r))) (:echem.FuelCellRun/effPct dat)))
      (is (= (Math/round (double (:stack-kW r))) (:echem.FuelCellRun/stackKW dat))))))

(deftest run-at-power-unreachable-is-honest
  (testing "an infeasible demand logs the peak point with feasible=false — never fabricated"
    (let [r (fc/run-at-power {:case/id "overdemand"} 5000.0)]
      (is (false? (:feasible r)))
      (is (= (:stack-kW r) (:peak-kW r))))))

(deftest run-at-power-registered-on-contract
  (testing "the cae.solver dispatch routes a power-targeted case to run-at-power"
    (let [r (cae/solve {:solver {:kind :rom-fc-at-power}
                        :p-kw 80.0})]
      (is (= :rom-fc-at-power (:solver r)))
      (is (:feasible r))
      (is (< (Math/abs (- (:stack-kW r) 80.0)) 1.0e-3)))))
