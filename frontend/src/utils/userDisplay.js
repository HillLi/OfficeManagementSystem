export const displayUserName = (users, userId) => {
  if (userId === null || userId === undefined || userId === '') return '-'
  return users.find((user) => user.id === userId)?.realName || `#${userId}`
}

export const originatorIdOf = (row, instances = []) => {
  if (!row) return null
  if (row.applicantId !== undefined && row.applicantId !== null) return row.applicantId
  if (row.organizerId !== undefined && row.organizerId !== null) return row.organizerId
  if (row.starterId !== undefined && row.starterId !== null) return row.starterId

  const matchedInstance = instances.find((instance) => {
    if (row.instanceId !== undefined && row.instanceId !== null) {
      return instance.id === row.instanceId
    }
    return instance.bizType === row.bizType && instance.bizId === row.bizId
  })
  return matchedInstance?.starterId ?? null
}

export const originatorNameOf = (row, users, instances = []) =>
  displayUserName(users, originatorIdOf(row, instances))
