(ns generator.renderer.util)

(defn iso-to-relative
  [iso-date]
  (let [zone (java.time.ZoneId/of "Europe/Helsinki")
        todayOff (java.time.OffsetDateTime/now zone)
        today (.toLocalDate (.atZoneSameInstant todayOff (java.time.ZoneId/systemDefault)))
        thenOff (java.time.OffsetDateTime/parse iso-date)
        then (.toLocalDate (.atZoneSameInstant thenOff (java.time.ZoneId/systemDefault)))
        period (java.time.Period/between then today)
        days (.getDays period)
        weeks (int (Math/floor (/ days 7)))
        months (.getMonths period)
        years (.getYears period)]
    (cond
      (= 1 years) (str years " year ago")
      (< 1 years) (str years " years ago")
      (= 1 months) (str months " month ago")
      (< 1 months) (str months " months ago")
      (= 1 weeks) (str weeks " week ago")
      (< 1 weeks) (str weeks " weeks ago")
      (= 1 days) "Yesterday"
      (< 1 days) (str days " days ago")
      (= 0 days) "Today")))
