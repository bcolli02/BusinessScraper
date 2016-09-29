package com.bcollins.tablescraper

object States extends Enumeration {

  type State = Value
  val AL, AK, AA, AE, AP, AS, AZ, AR, CA, CO, CT, DE,
    DC, FM, FL, GA, GU, HI, ID, IL, IN, IA, KS, KY, LA,
    MH, MA, ME, MD, MS, MI, MN, MO, MT, NE, NV, NH, NJ,
    NM, NY, NC, ND, MP, OH, OK, OR, PW, PA, PR, RI, SC,
    SD, TN, TX, UT, VT, VA, VI, WA, WV, WI, WY = Value
}

object BusinessTypes extends Enumeration {

  type BusinessType = Value
  val Vet = Value
}
