#include <bits/stdc++.h>

using namespace std;
using ll = long long;

int CountNumberOfPairs(int a, int b, ll n, int k) {
  // TODO: implement this method to determine the number of pairs modulo 10^9+7
  int number_of_pairs = 0;

  return number_of_pairs;
}

int main() {
  int test_case_count;
  cin >> test_case_count;
  int a, b, k;
  ll n;

  for (int tc = 1; tc <= test_case_count; ++tc) {
    cin >> a >> b >> n >> k;
    cout << "Case #" << tc << ": " << CountNumberOfPairs(a, b, n, k) << "\n";
  }
  return 0;
}
