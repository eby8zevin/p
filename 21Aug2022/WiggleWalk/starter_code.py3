def main():
  test_cases = int(input())
  for test_case in range(1, test_cases + 1):
    N, R, C, Sr, Sc = map(int, input().split())
    instructions = input()

    final_r, final_c = end_position(N, R, C, Sr, Sc, instructions)
    print(f'Case #{test_case}: {final_r} {final_c}')

def end_position(N, R, C, Sr, Sc, instructions):
  # TODO: Complete this function and return the final position (row, column) of
  # the robot
  final_r, final_c = 0, 0

  return final_r, final_c

if __name__ == '__main__':
  main()
