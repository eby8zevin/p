#include <bits/stdc++.h>
using namespace std;

pair<int, int> EndPosition(int N, int R, int C, int Sr, int Sc,
                           string instructions) {
  pair<int, int> p = {0, 0};
  // TODO: implement this method to return the row and column where the robot
  // finishes.
  return p;
}

int main() {
  int T;
  cin >> T;
  for (int t = 1; t <= T; ++t) {
    int N, R, C, Sr, Sc;
    string instructions;
    cin >> N >> R >> C >> Sr >> Sc >> instructions;
    auto ans = EndPosition(N, R, C, Sr, Sc, instructions);
    cout << "Case #" << t << ": " << ans.first << " " << ans.second << endl;
  }
}
